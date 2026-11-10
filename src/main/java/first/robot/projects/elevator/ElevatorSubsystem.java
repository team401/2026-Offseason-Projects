package first.robot.projects.elevator;

import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;
import static org.wpilib.units.Units.Seconds;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import coppercore.math.Lazy;
import coppercore.parameter_tools.LoggedTunableNumber;
import coppercore.wpilib_interface.MonitoredSubsystem;
import coppercore.wpilib_interface.subsystems.motors.MotorIO;
import coppercore.wpilib_interface.subsystems.motors.MotorInputsAutoLogged;
import first.robot.constants.JsonConstants;
import org.wpilib.driverstation.internal.DriverStationBackend;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Distance;

public class ElevatorSubsystem extends MonitoredSubsystem {

  // Motor IOs and inputs
  private final MotorIO leadMotor;
  private final MotorIO followerMotor;

  private final MotorInputsAutoLogged leadMotorInputs = new MotorInputsAutoLogged();
  private final MotorInputsAutoLogged followerMotorInputs = new MotorInputsAutoLogged();

  public void monitoredPeriodic() {}

  // State machine states
  private final StateMachine<ElevatorSubsystem> stateMachine;

  private final State<ElevatorSubsystem> homingWaitingForButtonState;
  private final State<ElevatorSubsystem> homingWaitingForMovementState;
  private final State<ElevatorSubsystem> homingWaitingForStoppingState;
  private final State<ElevatorSubsystem> idleState;
  private final State<ElevatorSubsystem> targetPositionState;
  private final State<ElevatorSubsystem> testModeState;

  // Test mode

  // TestModeManager<TestMode> testModeManager = new TestModeManager<TestMode>("Elevator",
  // TestMode.class);

  // Tunable numbers
  private final Lazy<LoggedTunableNumber> elevatorKP;
  private final Lazy<LoggedTunableNumber> elevatorKI;
  private final Lazy<LoggedTunableNumber> elevatorKD;

  private final Lazy<LoggedTunableNumber> elevatorKS;
  private final Lazy<LoggedTunableNumber> elevatorKV;
  private final Lazy<LoggedTunableNumber> elevatorKA;
  private final Lazy<LoggedTunableNumber> elevatorKG;

  private final Lazy<LoggedTunableNumber> elevatorExpoKV;
  private final Lazy<LoggedTunableNumber> elevatorExpoKA;

  private final Lazy<LoggedTunableNumber> elevatorTuningSetpointDegrees;
  private final Lazy<LoggedTunableNumber> elevatorTuningAmps;
  private final Lazy<LoggedTunableNumber> elevatorTuningVolts;

  // State variables: tbd
  private Boolean isHomingSwitchPressed;
  private Boolean isTargetingPosition;

  private Distance currentTargetingPosition;

  public ElevatorSubsystem(MotorIO leadMotor, MotorIO followerMotor) {
    this.leadMotor = leadMotor;
    this.followerMotor = followerMotor;

    // Register state machine and define states
    stateMachine = new StateMachine<>(this);

    // Add monitoring system stuff

    homingWaitingForButtonState =
        stateMachine.registerState(
            new State<ElevatorSubsystem>() {
              @Override
              public void periodic(
                  StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                elevator.homingWaitForButton();
              }
            });

    homingWaitingForMovementState =
        stateMachine.registerState(
            new State<ElevatorSubsystem>() {
              @Override
              public void periodic(
                  StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                elevator.applyHomingVoltage();
              }
            });

    homingWaitingForStoppingState =
        stateMachine.registerState(
            new State<ElevatorSubsystem>() {
              @Override
              public void periodic(
                  StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                elevator.applyHomingVoltage();

                if (!elevator.isEitherMoving()) {
                  elevator.setHomePosition();
                }
              }
            });

    idleState =
        stateMachine.registerState(
            new State<ElevatorSubsystem>() {
              @Override
              public void periodic(
                  StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                elevator.coast();
              }
            });

    targetPositionState =
        stateMachine.registerState(
            new State<ElevatorSubsystem>() {
              @Override
              public void periodic(
                  StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                // implement
              }
            });

    testModeState =
        stateMachine.registerState(
            new State<ElevatorSubsystem>() {
              @Override
              public void periodic(
                  StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                controlToCurrentSetPosition();
              }
            });

    homingWaitingForButtonState
        .when(elevator -> elevator.isHomingSwitchPressed(), "Homing switch is pressed")
        .transitionTo(idleState);
    homingWaitingForButtonState
        .when(elevator -> DriverStationBackend.isEnabled(), "Robot is enabled")
        .transitionTo(homingWaitingForMovementState);

    homingWaitingForMovementState
        .when(elevator -> elevator.isEitherMoving(), "At least one motor is moving")
        .transitionTo(homingWaitingForStoppingState);
    homingWaitingForMovementState
        .whenTimeout(Seconds.of(2))
        .transitionTo(homingWaitingForStoppingState);

    homingWaitingForStoppingState
        .when(elevator -> !elevator.isEitherMoving(), "Neither motor is running")
        .transitionTo(idleState);

    idleState
        .when(elevator -> elevator.isTargetingPosition(), "Targeting position")
        .transitionTo(targetPositionState);

    targetPositionState
        .when(elevator -> !elevator.isTargetingPosition(), "Stopped targeting position")
        .transitionTo(idleState);

    stateMachine.setState(homingWaitingForButtonState);

    // Initialize tunables numbers
    // TODO: add Jsonconstants for these values for test modes
    elevatorKP =
        new Lazy<>(
            () ->
                new LoggedTunableNumber(
                    "ElevatorTunables/ElevatorKP", JsonConstants.elevatorConstants.elevatorKP));
    elevatorKI =
        new Lazy<>(
            () ->
                new LoggedTunableNumber(
                    "ElevatorTunables/ElevatorKI", JsonConstants.elevatorConstants.elevatorKI));
    elevatorKD =
        new Lazy<>(
            () ->
                new LoggedTunableNumber(
                    "ElevatorTunables/ElevatorKD", JsonConstants.elevatorConstants.elevatorKD));

    elevatorKS =
        new Lazy<>(
            () ->
                new LoggedTunableNumber(
                    "ElevatorTunables/ElevatorKS", JsonConstants.elevatorConstants.elevatorKS));
    elevatorKG =
        new Lazy<>(
            () ->
                new LoggedTunableNumber(
                    "ElevatorTunables/ElevatorKG", JsonConstants.elevatorConstants.elevatorKG));
    elevatorKV =
        new Lazy<>(
            () ->
                new LoggedTunableNumber(
                    "ElevatorTunables/ElevatorKV", JsonConstants.elevatorConstants.elevatorKV));
    elevatorKA =
        new Lazy<>(
            () ->
                new LoggedTunableNumber(
                    "ElevatorTunables/ElevatorKA", JsonConstants.elevatorConstants.elevatorKA));

    elevatorExpoKV =
        new Lazy<>(
            () ->
                new LoggedTunableNumber(
                    "ElevatorTunables/ElevatorExpoKV",
                    JsonConstants.elevatorConstants.elevatorExpoKV));
    elevatorExpoKA =
        new Lazy<>(
            () ->
                new LoggedTunableNumber(
                    "ElevatorTunables/ElevatorExpoKA",
                    JsonConstants.elevatorConstants.elevatorExpoKA));

    elevatorTuningAmps =
        new Lazy<>(() -> new LoggedTunableNumber("ElevatorTunables/ElevatorTuningAmps", 0));
    elevatorTuningSetpointDegrees =
        new Lazy<>(
            () -> new LoggedTunableNumber("ElevatorTunables/ElevatorTuningSetpointDegrees", 0));
    elevatorTuningVolts =
        new Lazy<>(() -> new LoggedTunableNumber("ElevatorTunables/ElevatorTuningVolts", 0));

    // add dep order stuff and autologoutputmanager
  }

  public void coast() {
    leadMotor.controlCoast();
    followerMotor.controlCoast();
  }

  public void homingWaitForButton() {
    if (isHomingSwitchPressed) {
      setHomePosition();
    }
  }

  public void setHomePosition() {
    leadMotor.setCurrentPosition(Radians.of(0.0));
    followerMotor.setCurrentPosition(Radians.of(0.0));
  }

  public boolean isHomingSwitchPressed() {
    return isHomingSwitchPressed;
  }

  public void setIsHomingSwitchPressed(boolean isHomingSwitchPressed) {
    this.isHomingSwitchPressed = isHomingSwitchPressed;
  }

  public void applyHomingVoltage() {
    leadMotor.controlOpenLoopVoltage(JsonConstants.elevatorConstants.homingVoltage);
    followerMotor.controlOpenLoopVoltage(JsonConstants.elevatorConstants.homingVoltage);
  }

  public AngularVelocity getVelocityLeadMotor() {
    return RadiansPerSecond.of(leadMotorInputs.velocityRadiansPerSecond);
  }

  public AngularVelocity getVelocityFollowerMotor() {
    return RadiansPerSecond.of(followerMotorInputs.velocityRadiansPerSecond);
  }

  public boolean isEitherMoving() {
    return getVelocityLeadMotor().gte(JsonConstants.elevatorConstants.homingMovementThreshold)
        || getVelocityFollowerMotor().gte(JsonConstants.elevatorConstants.homingMovementThreshold);
  }

  public boolean isTargetingPosition() {
    return isTargetingPosition;
  }

  public void setTargetingPosition(Distance height) {
    isTargetingPosition = true;
    currentTargetingPosition = height;
  }

  public void stopTargetingPosition() {
    isTargetingPosition = false;
  }

  public void controlToCurrentSetPosition() {
    if (currentTargetingPosition != null) {
      // implement
    }
  }
}
