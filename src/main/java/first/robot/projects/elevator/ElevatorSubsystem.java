package first.robot.projects.elevator;

import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;
import static org.wpilib.units.Units.Seconds;
import static org.wpilib.units.Units.Volts;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import coppercore.wpilib_interface.MonitoredSubsystem;
import coppercore.wpilib_interface.subsystems.motors.MotorIO;
import coppercore.wpilib_interface.subsystems.motors.MotorInputsAutoLogged;
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

  // Tunable Numbers (ADD!!!!!)

  // State variables: tbd
  private Boolean isHomingSwitchPressed;
  private Boolean isTargetingPosition;

  private Distance currentTargetingPosition;

  public ElevatorSubsystem(MotorIO leadMotor, MotorIO followerMotor) {
    this.leadMotor = leadMotor;
    this.followerMotor = followerMotor;

    // Register state machine and define states
    stateMachine = new StateMachine<>(this);

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
    ;

    idleState
        .when(elevator -> elevator.isTargetingPosition(), "Targeting position")
        .transitionTo(targetPositionState);
    ;

    targetPositionState
        .when(elevator -> !elevator.isTargetingPosition(), "Stopped targeting position")
        .transitionTo(idleState);
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
    leadMotor.controlOpenLoopVoltage(Volts.of(0.1));
    followerMotor.controlOpenLoopVoltage(Volts.of(0.1));
  }

  public AngularVelocity getVelocityLeadMotor() {
    return RadiansPerSecond.of(leadMotorInputs.velocityRadiansPerSecond);
  }

  public AngularVelocity getVelocityFollowerMotor() {
    return RadiansPerSecond.of(followerMotorInputs.velocityRadiansPerSecond);
  }

  public boolean isEitherMoving() {
    return getVelocityLeadMotor().abs(RadiansPerSecond) >= 0.2
        || getVelocityFollowerMotor().abs(RadiansPerSecond) >= 0.2;
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
