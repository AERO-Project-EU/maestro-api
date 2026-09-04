package eu.orchestrator.elasticity.spi.service.orchestrator;

import java.io.*;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 25/7/2019
 */
public class FileInteractions {

    public static String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));

        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();

            }

            return sb.toString();

        } finally {
            br.close();

        }

    }

    public static void writeFile(String fileName, String body) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        try {
            writer.write(body);
            writer.close();
        } finally {
            writer.close();

        }
    }
}
