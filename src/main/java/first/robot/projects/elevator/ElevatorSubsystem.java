package first.robot.projects.elevator;

import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import coppercore.wpilib_interface.MonitoredSubsystem;
import coppercore.wpilib_interface.subsystems.motors.MotorIO;
import coppercore.wpilib_interface.subsystems.motors.MotorInputsAutoLogged;

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

    public ElevatorSubsystem(MotorIO leadMotor, MotorIO followerMotor) {
        this.leadMotor = leadMotor;
        this.followerMotor = followerMotor;

        // Register state machine and define states
        stateMachine = new StateMachine<>(this);

        homingWaitingForButtonState = stateMachine.registerState(new State<ElevatorSubsystem>() {
            @Override
            public void periodic(StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                // implement
            }
        });        
        
        homingWaitingForMovementState = stateMachine.registerState(new State<ElevatorSubsystem>() {
            @Override
            public void periodic(StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                // implement
            }
        });        
        
        homingWaitingForStoppingState = stateMachine.registerState(new State<ElevatorSubsystem>() {
            @Override
            public void periodic(StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                // implement
            }
        });        
        
        idleState = stateMachine.registerState(new State<ElevatorSubsystem>() {
            @Override
            public void periodic(StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                elevator.coast();
            }
        });        
        
        targetPositionState = stateMachine.registerState(new State<ElevatorSubsystem>() {
            @Override
            public void periodic(StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                // implement
            }
        });

        testModeState = stateMachine.registerState(new State<ElevatorSubsystem>() {
            @Override
            public void periodic(StateMachine<ElevatorSubsystem> stateMachine, ElevatorSubsystem elevator) {
                // implement
            }
        });
    }

    public void coast() {
        leadMotor.controlCoast();
        followerMotor.controlCoast();
    }
}
