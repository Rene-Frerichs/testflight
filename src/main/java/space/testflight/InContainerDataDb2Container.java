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

import org.testcontainers.containers.Db2Container;
import org.testcontainers.containers.InternetProtocol;

public class InContainerDataDb2Container extends Db2Container implements DefaultTaggableContainer<Db2Container> {

  public InContainerDataDb2Container() {
    init();
  }

  public InContainerDataDb2Container(String image) {
    super(image);
    init();
  }

  private void init() {
    addEnv("PERSISTENT_HOME", "false");
    acceptLicense();
  }

  @Override
  public int getContainerPort() {
    return DB2_PORT;
  }

  @Override
  public String getDefaultImageName() {
    return DEFAULT_DB2_IMAGE_NAME;
  }

  @Override
  public void addFixedPort(int hostPort, int containerPort) {
    super.addFixedExposedPort(hostPort, containerPort, InternetProtocol.TCP);
  }
}
