package first.robot.constants;

import org.wpilib.system.Filesystem;

import coppercore.parameter_tools.json.JSONHandler;
import coppercore.parameter_tools.json.JSONSyncConfigBuilder;
import coppercore.parameter_tools.json.adapters.OptionalTypeAdapterFactory;
import coppercore.parameter_tools.path_provider.EnvironmentHandler;
import coppercore.wpilib_interface.controllers.Controllers;

public class JsonConstants {
  public static EnvironmentHandler environmentHandler;
  public static JSONHandler jsonHandler;



  public static JSONHandler loadConstants() {

    environmentHandler =
        EnvironmentHandler.getEnvironmentHandler(
            Filesystem.getDeployDirectory().toPath().resolve("constants/config.json").toString());

    var jsonSyncSettings = new JSONSyncConfigBuilder();

    Controllers.applyControllerConfigToBuilder(jsonSyncSettings);

    jsonSyncSettings.addJsonTypeAdapterFactory(new OptionalTypeAdapterFactory());

    var pathProvider = environmentHandler.getEnvironmentPathProvider();

    System.out.println("[JsonConstants] Environment name: " + pathProvider.getEnvironmentName());
    jsonHandler = new JSONHandler(jsonSyncSettings.build(), pathProvider);

    operatorConstants = jsonHandler.getObject(new OperatorConstants(), "OperatorConstants.json");
    featureFlags = jsonHandler.getObject(new FeatureFlags(), "FeatureFlags.json");
    canBusAssignment = jsonHandler.getObject(new CANBusAssignment(), "CANBusAssignment.json");

    controllers =
        jsonHandler.getObject(new Controllers(), operatorConstants.controllerBindingsFile);

    return jsonHandler;
  }

  public static FeatureFlags featureFlags;
  public static CANBusAssignment canBusAssignment;

  public static Controllers controllers;  
  public static OperatorConstants operatorConstants;
}
