package eu.orchestrator.repository.api;

import java.io.File;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface IFilesystemService {

  boolean createFolderIntoPath(String path);

  File[] retrieveAllFileFromPath(String path);

  boolean storeFileIntoFolder(MultipartFile file, String path);
}
