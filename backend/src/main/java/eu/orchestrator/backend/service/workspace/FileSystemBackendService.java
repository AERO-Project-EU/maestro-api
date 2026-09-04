package eu.orchestrator.backend.service.workspace;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.FileTO;
import eu.orchestrator.repository.api.IFilesystemService;
import eu.orchestrator.repository.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class FileSystemBackendService {

    private static final Logger logger = Logger.getLogger(FileSystemBackendService.class.getName());

    @Autowired
    private IFilesystemService iFileSystemService;

    @Value("${nfs.path}")
    private String rootPath;


    public Page<List<FileTO>> fetchFiles(Pageable pageable, FileTO file, User authenticatedUser) {
        String pathAsString = rootPath + "/" + authenticatedUser.getOrganization().getName() + file.getPath();
        File[] listOfFiles = iFileSystemService.retrieveAllFileFromPath(pathAsString);
        List<FileTO> fileTOs = new ArrayList<>();
        for (File listOfFile : listOfFiles) {
            FileTO fileTO = new FileTO();
            fileTO.setName(listOfFile.getName());
            String pathForUI = listOfFile.getAbsolutePath().replace(rootPath + "/" + authenticatedUser.getOrganization().getName(), "");
            fileTO.setPath(pathForUI);
            if (listOfFile.isFile()) {
                fileTO.setType("FILE");
                double bytes = listOfFile.length();
                double kilobytes = (bytes / 1024);
                double megabytes = (kilobytes / 1024);
                DecimalFormat decimalFormat = new DecimalFormat("##.00");
                fileTO.setSize(decimalFormat.format(megabytes) + " MB");
            } else if (listOfFile.isDirectory()) {
                fileTO.setType("DIRECTORY");
            }
            fileTOs.add(fileTO);
        }
        fileTOs = fileTOs.stream().sorted(Comparator.comparing(FileTO::getType)).collect(Collectors.toList());
        return new PageImpl(fileTOs, pageable, fileTOs.size());
    }

    public void createDirectory(FileTO file, User authenticatedUser) {
        String pathAsString = rootPath + "/" + authenticatedUser.getOrganization().getName() + file.getPath();
        if (!iFileSystemService.createFolderIntoPath(pathAsString)) {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public void handleFileUpload(MultipartFile file, String path, User authenticatedUser) {
        String pathAsString = rootPath + "/" + authenticatedUser.getOrganization().getName() + path;
        logger.log(Level.INFO, "Store file: " + pathAsString + "/" + file.getOriginalFilename());
        iFileSystemService.storeFileIntoFolder(file, pathAsString);
    }
}
