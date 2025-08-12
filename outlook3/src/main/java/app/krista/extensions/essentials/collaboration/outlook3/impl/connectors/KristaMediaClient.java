package app.krista.extensions.essentials.collaboration.outlook3.impl.connectors;

import app.krista.ksdk.files.FileHandle;
import app.krista.ksdk.files.FileRepository;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class KristaMediaClient {

    private static final String zipDir = "/tmp/";
    @Inject
    private FileRepository fileRepository;

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
        return uploadFileToRepository(file);
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
        if (fileName.lastIndexOf(".") == -1) {
            return false;
        }
        String fileExtension = getFileExtension(fileName);
        return fileRepository.getBlackListedFileExtensions().contains(fileExtension);
    }

    /**
     * Converts an input stream to a file.
     *
     * @param inputStream The input stream to be converted.
     * @param input       The file to write the input stream content to.
     * @return The file with the content of the input stream.
     * @throws IOException If an I/O error occurs.
     */
    private File convertInputStreamToFile(InputStream inputStream, File input) throws IOException {
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(input))) {
            byte[] buffer = new byte[4096];
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
        if (fileName.contains(".")) {
            return fileName.substring((fileName.lastIndexOf(".") + 1));
        }
        throw new IllegalArgumentException("Unsupported file format");
    }

    /**
     * Compresses a file into a zip archive.
     *
     * @param zipFilePath  The path where the zip file will be created.
     * @param dirPathToZip The path of the directory to be zipped.
     * @throws IOException If an I/O error occurs.
     */
    public static void compressFile(String zipFilePath, String dirPathToZip) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            File fileToZip = new File(dirPathToZip);

            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
            }

            zipOut.closeEntry();
        }
    }

    /**
     * Uploads a file to Krista's media server. it will take java.io.File object as input and returns krista's file object
     *
     * @param file The file to be uploaded.
     * @return The Krista file object.
     * @throws IOException If an I/O error occurs.
     */
    public app.krista.model.base.File toKristaZipFile(File file) throws IOException {
        String sanitizedFileName = sanitizeFileName(file.getName());
        File sanitizedFile = file;

        if (!sanitizedFileName.equals(file.getName())) {
            // Use temp directory if parent is null
            String parentDir = file.getParent() != null ? file.getParent() : zipDir;
            String tempPath = parentDir + "/" + sanitizedFileName;
            sanitizedFile = new File(tempPath);
            Files.copy(file.toPath(), sanitizedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        String baseName = sanitizedFile.getName();
        int lastDotIndex = baseName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            baseName = baseName.substring(0, lastDotIndex);
        }
        String zipFilePath = zipDir + baseName + ".zip";
        compressFile(zipFilePath, sanitizedFile.getAbsolutePath());
        File zipFile = new File(zipFilePath);
        return uploadFileToRepository(zipFile);
    }

    /**
     * Sanitizes the file name by replacing problematic characters with underscores.
     *
     * @param fileName The original file name.
     * @return The sanitized file name.
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unnamed_file";

        // Replace problematic characters with underscores
        return fileName.replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("[<>:\"/\\\\|?*]", "_")
                .trim();
    }

    /**
     * Common method to upload a file to the repository
     */
    private app.krista.model.base.File uploadFileToRepository(File file) throws IOException {
        try (final FileHandle fileHandle = fileRepository.createNewFileByName(file.getName())) {
            FileInputStream stream = new FileInputStream(file);
            fileHandle.setContent(stream);
            return fileHandle.getFile();
        }
    }
}