package first.robot.constants;

import coppercore.parameter_tools.json.annotations.JSONExclude;
import org.littletonrobotics.junction.Logger;

/**
 * FeatureFlags contains the set of flags that enable or disable each subsystem of the robot.
 *
 * <p>When writing a new subsystem, ensure that the program can function with any combination of
 * enabled/disabled subsystems without crashes.
 */
public class FeatureFlags {
  public final Boolean useElevatorProject = true;
  public final Boolean useHomingSwitch = false;
  public final Boolean useTuningServer = false;
  @JSONExclude public static final Boolean usePhoenixDiagnosticServer = false;
  public final Boolean logPeriodicTiming = false;

  /** Print the current state of the feature flags and writes them to the log */
  public void logFlags() {
    System.out.println("Feature flags:");

    System.out.println(" - useElevatorProject: " + useElevatorProject);
    System.out.println(" - useHomingSwitch: " + useHomingSwitch);
    System.out.println(" - useTuningServer: " + useTuningServer);
    System.out.println(" - usePhoenixDiagnosticServer: " + usePhoenixDiagnosticServer);
    System.out.println(" - logPeriodicTiming: " + logPeriodicTiming);

    Logger.recordOutput("FeatureFlags/useElevatorProject", useElevatorProject);
    Logger.recordOutput("FeatureFlags/useHomingSwitch", useHomingSwitch);
    Logger.recordOutput("FeatureFlags/useTuningServer", useTuningServer);
    Logger.recordOutput("FeatureFlags/usePhoenixDiagnosticServer", usePhoenixDiagnosticServer);
    Logger.recordOutput("FeatureFlags/logPeriodicTiming", logPeriodicTiming);
  }
}
