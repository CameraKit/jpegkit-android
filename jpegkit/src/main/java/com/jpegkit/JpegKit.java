package com.jpegkit;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class JpegKit {

    private static void writeFile(Jpeg jpeg, File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(jpeg.getJpegBytes());
        outputStream.close();
    }

    @NonNull
    public static JpegFile writeToInternalFilesDirectory(@NonNull Context context, @Nullable String filePath,
                                                         @NonNull String fileName, @NonNull Jpeg jpeg) throws IOException {
        File directory = context.getFilesDir();
        if (filePath != null && filePath.length() > 0) {
            directory = new File(directory, filePath);

            if (!directory.exists() && !directory.mkdirs()) {
                directory = context.getFilesDir();
            }
        }

        File targetFile = new File(directory, fileName);
        writeFile(jpeg, targetFile);

        return new JpegFile(targetFile);
    }

    @NonNull
    public static JpegFile writeToInternalCacheDirectory(@NonNull Context context, @Nullable String filePath,
                                                         @NonNull String fileName, @NonNull Jpeg jpeg) throws IOException {
        File directory = context.getCacheDir();
        if (filePath != null && filePath.length() > 0) {
            directory = new File(directory, filePath);

            if (!directory.exists() && !directory.mkdirs()) {
                directory = context.getCacheDir();
            }
        }

        File targetFile = new File(directory, fileName);
        writeFile(jpeg, targetFile);

        return new JpegFile(targetFile);
    }

    @NonNull
    public static JpegFile writeToExternalPrivateDirectory(@NonNull Context context, @Nullable String filePath,
                                                           @NonNull String fileName, @NonNull Jpeg jpeg) throws IOException {
        if (isExternalStorageWritable()) {
            File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (filePath != null && filePath.length() > 0) {
                directory = new File(directory, filePath);

                if (!directory.mkdirs()) {
                    return null;
                }
            }

            File targetFile = new File(directory, fileName);
            writeFile(jpeg, targetFile);

            return new JpegFile(targetFile);
        }

        return null;
    }

    @NonNull
    public static JpegFile writeToExternalPublicDirectory(@NonNull Context context, @Nullable String filePath,
                                                          @NonNull String fileName, @NonNull Jpeg jpeg) throws IOException {
        if (isExternalStorageWritable()) {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (filePath != null && filePath.length() > 0) {
                directory = new File(directory, filePath);

                if (!directory.mkdirs()) {
                    return null;
                }
            }

            File targetFile = new File(directory, fileName);
            writeFile(jpeg, targetFile);

            return new JpegFile(targetFile);
        }

        return null;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

}
