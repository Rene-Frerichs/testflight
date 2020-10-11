/*
 * Copyright 2020 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package space.testflight;

import static space.testflight.Flyway.BackupStrategy.TAG;
import static space.testflight.Flyway.BackupStrategy.VOLUME;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static org.testcontainers.containers.Db2Container.DEFAULT_DB2_IMAGE_NAME;
import static org.testcontainers.containers.PostgreSQLContainer.IMAGE;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.model.Image;

@ExtendWith(FlywayExtension.class)
@Target({ANNOTATION_TYPE, TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Flyway {
  DatabaseType database() default DatabaseType.POSTGRESQL;
  String dockerImage() default "";
  String[] testDataScripts() default {};

  enum DatabaseType {
    POSTGRESQL(IMAGE, TAG, "/var/lib/postgresql/data"),
    DB2(DEFAULT_DB2_IMAGE_NAME, VOLUME, "/database");

    private String image;
    private BackupStrategy strategy;
    private String path;

    DatabaseType(String databaseImage, BackupStrategy backupStrategy, String containerDataPath) {
      image = databaseImage;
      strategy = backupStrategy;
      path = containerDataPath;
    }

    public String getImage() {
      return image;
    }

    public BackupStrategy getBackupStrategy() {
      return strategy;
    }

    public String getContainerDataPath() {
      return path;
    }

    public boolean isBackupAvailable(String backupIdentifier) {
      switch (strategy) {
        case TAG:
          return isBackupImageAvailable(backupIdentifier);
        default:
          return isBackupDirectoryAvailable(backupIdentifier);
      }
    }

    public String getBackupImage(String backupIdentifier) {
      switch (strategy) {
        case TAG:
          return getImage() + ":" + backupIdentifier;
        default:
          return getImage();
      }
    }

    public String getBackupDirectory(String backupIdentifier) {
      return "target" + System.getProperty("file.separator") + backupIdentifier;
    }

    public File getBackupFile(String backupIdentifier) {
      return new File(getBackupDirectory(backupIdentifier));
    }

    private boolean isBackupImageAvailable(String backupIdentifier) {
      List<Image> imageList = DockerClientFactory.lazyClient()
        .listImagesCmd()
        .exec();

      return imageList.stream()
        .map(i -> i.getRepoTags())
        .filter(Objects::nonNull)
        .flatMap(Arrays::stream)
        .anyMatch(t -> t.equals(getBackupImage(backupIdentifier)));

    }

    private boolean isBackupDirectoryAvailable(String backupIdentifier) {
      File directory = new File(getBackupDirectory(backupIdentifier));
      return directory.exists() && directory.isDirectory();
    }
  }

  enum BackupStrategy {
    TAG, VOLUME;
  }
}
