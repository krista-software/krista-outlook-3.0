package app.krista.extensions.essentials.collaboration.outlook3.impl.connectors;

import app.krista.ksdk.files.FileHandle;
import app.krista.ksdk.files.FileRepository;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class KristaMediaClient {

    private static final String zipDir = "/tmp/";
    @Inject
    private FileRepository fileRepository;

    private final List<String> unSupportedFileFormats = Arrays.asList("html", "php5", "pht", "phtml", "shtml", "asa", "cer", "asax", "swf", "xap", "jsp", "exe", "js");

    /**
     * Uploads a file to Krista's media server. it will take java.io.File object as input and returns krista's file object
     *
     * @param file The file to be uploaded.
     * @return The Krista file object.
     * @throws IOException If an I/O error occurs.
     */
    public app.krista.model.base.File toKristaFile(File file) throws IOException {
        if (isUnsupportedFileFormat(file.getName())) {
            String zipFilePath = zipDir + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".zip";
            compressFile(zipFilePath, file.getAbsolutePath());
            file = new File(zipFilePath);
        }
        try (final FileHandle fileHandle = fileRepository.createNewFileByName(file.getName())) {
            fileHandle.setContent(new FileInputStream(file));
            return fileHandle.getFile();
        }
    }

    /**
     * Downloads a file from Krista's media server. it will take krista's file object as input and returns java.io.File object
     *
     * @param file The Krista file object to be downloaded.
     * @return The downloaded Java file object.
     * @throws IOException If an I/O error occurs.
     */
    public File toJavaFile(app.krista.model.base.File file) throws IOException {
        try (FileHandle fileHandle = fileRepository.getFile(file)) {
            InputStream content = fileHandle.getContent();
            final File input = new File(file.getFileName());
            return convertInputStreamToFile(content, input);
        }
    }

    /**
     * Checks if the file format is unsupported.
     *
     * @param fileName The name of the file.
     * @return True if the file format is unsupported, otherwise false.
     */
    private boolean isUnsupportedFileFormat(String fileName) {
        if(fileName.lastIndexOf(".") == -1){
            return false;
        }
        String fileExtension = getFileExtension(fileName);
        return unSupportedFileFormats.contains(fileExtension);
    }

    /**
     * Converts an input stream to a file.
     *
     * @param inputStream The input stream to be converted.
     * @param input The file to write the input stream content to.
     * @return The file with the content of the input stream.
     * @throws IOException If an I/O error occurs.
     */
    private File convertInputStreamToFile(InputStream inputStream, File input) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(input)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return input;
    }

    /**
     * Retrieves the file extension from the file name.
     *
     * @param fileName The name of the file.
     * @return The file extension.
     * @throws IllegalArgumentException If the file format is unsupported.
     */
    private String getFileExtension(String fileName) {
        try {
            System.out.println("File extension Name :: " + fileName);
            if (fileName.contains(".")) {
                return fileName.substring((fileName.lastIndexOf(".") + 1));
            }
            return fileName;
        }catch (IllegalArgumentException cause){
            throw new IllegalArgumentException("Unsupported file format");
        }
    }

    /**
     * Compresses a file into a zip archive.
     *
     * @param zipFilePath The path where the zip file will be created.
     * @param dirPathToZip The path of the directory to be zipped.
     * @throws IOException If an I/O error occurs.
     */
    public static void compressFile(String zipFilePath, String dirPathToZip) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            File fileToZip = new File(dirPathToZip);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            zipOut.closeEntry();
        }
    }
}
