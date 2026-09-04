package eu.orchestrator.repository.service;

import eu.orchestrator.repository.api.IFilesystemService;
import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FilesystemService implements IFilesystemService {


  @Override
  public File[] retrieveAllFileFromPath(String path) {
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    return  listOfFiles;
  }

  @Override
  public boolean createFolderIntoPath(String newPath) {
    File directories = new File(newPath);
    return directories.mkdirs();
  }

  @Override
  public boolean storeFileIntoFolder(MultipartFile file, String path) {
    try {
      File fileToSave = new File(path + "/" + file.getOriginalFilename());
      file.transferTo(fileToSave);

      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
}
