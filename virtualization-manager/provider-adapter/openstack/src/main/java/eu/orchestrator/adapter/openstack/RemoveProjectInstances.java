package eu.orchestrator.adapter.openstack;

import eu.orchestrator.spi.model.CredentialsModel;
import eu.orchestrator.spi.model.InstanceModel;
import eu.orchestrator.spi.response.SPIResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoveProjectInstances {

  private static List<String> allProjects = new ArrayList(Arrays.asList(
      "unicorncas",
      "unicornmetadev",
      "unicornlstech",
      "unicornitlabs",
      "unicornsuite5",
      "UnicornInobeta",
      "UnicornUbiquitous",
      "UnicornSinapse",
      "UnicornBioAssist",
      "UnicornSirus",
      "UnicornPIXELRAM",
      "UnicornAniway",
      "UnicornNovatex",
      "UnicornRedikod",
      "UnicornLeanXcale",
      "unicorncas2",
      "UnicornUcy"));

  public static void main(String[] args) {

    for (String project : allProjects) {

      System.out.println("project --->" + project);
      OpenStackAdapter openStackAdapter = new OpenStackAdapter();

      CredentialsModel credentialsModel = new CredentialsModel();
      credentialsModel.setUsername(""); //TODO fill username
      credentialsModel.setPassword(""); //TODO fill password
      credentialsModel.setProject(project);
      credentialsModel.setEndpoint(System.getProperty("openstack.endpoint", "")); //TODO fill keystone endpoint
      credentialsModel.setDomain("default");

      SPIResponse spiResponse = openStackAdapter.getInstances(credentialsModel);

      List<InstanceModel> allInstancesPerProject = (List<InstanceModel>) spiResponse.getReturnobject();

      if (null != allInstancesPerProject){

        for (InstanceModel instanceModel: allInstancesPerProject) {
          openStackAdapter.removeInstance(credentialsModel, instanceModel);
        }
      }

    }

  }

}
