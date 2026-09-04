package topics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Panagiotis Parthenis
 */
public class TestColdboot {

  public static void main(String[] args) throws IOException {
/*    ArrayList<Path> allFiles = new ArrayList<>();

    try (Stream<Path> paths = Files.walk(Paths.get("/home/parthenis/Desktop"))) {
      paths
          .filter(Files::isRegularFile)
          .forEach(x ->allFiles.add(x));
    } catch (IOException e) {
      e.printStackTrace();
    }*/

    List list = Files.list(Paths.get("/data/maestro/policy-engine/rules"))
        .filter(Files::isRegularFile)
        .collect(Collectors.toList());

    for (int i = 0; i < list.size(); i++) {
      String filePath = list.get(i).toString();
      int startAt = filePath.lastIndexOf("/") + 1;
      int endAt = filePath.length() - 4;
      String name = filePath.substring(startAt, endAt);
      name = name.replace("_", ":");
      System.out.println("id ---> " + name);
      System.out.println("path ---> " + filePath);

      //drools.initService("path","id")
    }
  }

}
