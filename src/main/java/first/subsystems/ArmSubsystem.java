package first.subsystems;

import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.Degrees;

import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.Voltage;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;

import coppercore.math.Lazy;
import coppercore.parameter_tools.LoggedTunableNumber;
import coppercore.controls.state_machine.State;
import coppercore.controls.state_machine.StateMachine;
import coppercore.wpilib_interface.MonitoredSubsystem;
import coppercore.wpilib_interface.tuning.TestModeManager;
import coppercore.wpilib_interface.subsystems.motors.MotorIO;
import coppercore.wpilib_interface.subsystems.motors.MotorInputsAutoLogged;
import edu.wpi.first.wpilibj.DriverStation;

import org.wpilib.units.Units;

public class ArmSubsystem extends MonitoredSubsystem{
    
    TestModeManager<ArmTestMode> armTestModeManager = new TestModeManager<ArmTestMode>("Arm", ArmTestMode.class);

    StateMachine<ArmSubsystem> stateMachine; 

    protected MotorIO armMotorIO;

    protected MotorInputsAutoLogged armInputs;

    protected Angle targetArmAngle = Degrees.zero();
    protected Voltage holdVoltage = null; 

    private ArmDependencies dependencies = new ArmDependencies();

    boolean shouldBeInTestMode = false;



    private final StateMachine<ArmSubsystem> stateMachine;

    //Arm States
    private final State<ArmSubsystem> homingWaitForMovementState;
    private final State<ArmSubsystem> homingWaitForStopMovingState;
    private final State<ArmSubsystem> homingDoneState;
    private final State<ArmSubsystem> waitForButtonState;
    private final State<ArmSubsystem> controlToPositionState; 
    private final State<ArmSubsystem> testModeState;

    //Arm Tunable Numbers
    private final Lazy<LoggedTunableNumber> armKP;
    private final Lazy<LoggedTunableNumber> armKI;
    private final Lazy<LoggedTunableNumber> armKD;

    private final Lazy<LoggedTunableNumber> armKS;
    private final Lazy<LoggedTunableNumber> armKV;
    private final Lazy<LoggedTunableNumber> armKA;
    private final Lazy<LoggedTunableNumber> armKG;

    private final Lazy<LoggedTunableNumber> armExpoKV;
    private final Lazy<LoggedTunableNumber> armExpoKA;

    private final Lazy<LoggedTunableNumber> armTuningSetpointDegrees;
    private final Lazy<LoggedTunableNumber> armTuningAmps;
    private final Lazy<LoggedTunableNumber> armTuningVolts;

    /**
     * Constructor for the ArmSubsytem Class
     * @param armMotorIO The arms generic motor IO
     */
    public ArmSubsystem(MotorIO armMotorIO){
        this.armMotorIO = armMotorIO;

        this.stateMachine = new StateMachine<>(this);

        homingWaitForMovementState = stateMachine.registerState(
            new State<ArmSubsystem>() {
                @Override
                public void periodic(StateMachine<ArmSubsystem> stateMachine,
                ArmSubsystem arm) {
                    arm.armMotorIO.controlOpenLoopVoltage(JsonConstants.armConstants.homingVoltage);
                    
                    if (Math.abs(arm.armInputs.velocityRadiansPerSecond) 
                    > JsonConstants.armConstants.homingMovementThreshold.in(Units.RadiansPerSecond)) {
                        finish();
                    }

                }
            }
        );

        homingWaitForStopMovingState = stateMachine.registerState(
            new State<ArmSubsystem>() {
                @Override
                public void periodic(StateMachine<ArmSubsystem> stateMachine,
                ArmSubsystem arm) {
                    arm.armMotorIO.controlOpenLoopVoltage(JsonConstants.armConstants.homingVoltage);
                    
                    if (Math.abs(arm.armInputs.velocityRadiansPerSecond) 
                    < JsonConstants.armConstants.homingMovementThreshold.in(Units.RadiansPerSecond)) {
                        finish();
                    }

                }
            }
        );

        homingDoneState = stateMachine.registerState(
            new State<ArmSubsystem>() {
                @Override
                public void onEntry(StateMachine<ArmSubsystem> stateMachine, 
                ArmSubsystem arm) {
                    arm.armMotorIO.controlNeutral();
                }

                @Override
                public void periodic(StateMachine<ArmSubsystem> stateMachine,
                ArmSubsystem arm) {
                    arm.armMotorIO.setCurrentPosition(JsonConstants.armConstants.minArmAngle);
                    finish();
                }
            }
        );

        waitForButtonState = stateMachine.registerState(
            new State<ArmSubsystem>() {
                @Override
                public void periodic(StateMachine<ArmSubsystem> stateMachine,
                ArmSubsystem arm) {
                    if (shouldBeInTestMode){
                        arm.armMotorIO.setCurrentPositionAsZero();    
                        finish();
                    }
                }
            }
        );

        controlToPositionState = stateMachine.registerState(
            new State<ArmSubsystem>() {
                @Override
                public void periodic(StateMachine<ArmSubsystem> stateMachine,
                ArmSubsystem arm) {
                    arm.setPositionIfOutsideRange();
                    if (Radians.of(arm.armInputs.positionRadians)
                    .isNear(arm.targetArmAngle, JsonConstants.armConstants.armHoldAngleTolerance) 
                    && arm.holdVoltage != null){
                        arm.applyHoldVoltage();
                    } else{
                        arm.controlToTargetArmAngle();
                    }
                }
            }
        );

        waitForButtonState
            .whenFinished()
            .andWhen(arm -> arm.isInTestMode(), "Should be in test mode")
            .transitionTo(testModeState);
        
        homingDoneState
            .whenFinished()
            .andWhen(arm -> arm.isInTestMode(), "Should be in test mode")
            .transitionTo(testModeState);
        
        controlToPositionState
            .when(arm -> arm.isInTestMode(), "Should be in test mode")
            .transitionTo(testModeState);
        
        testModeState
            .whenFinished()
            .transitionTo(controlToPositionState);
        
        waitForButtonState
            .whenFinished()
            .transitionTo(controlToPositionState);
        
        waitForButtonState
            .when(DriverStation::isEnabled, "When robot is enabled and button has not been pressed")
            .transitionTo(homingWaitForMovementState);

        homingWaitForMovementState
            .when(DriverStation::isDisabled, "When robot is disabled during homing")
            .transitionTo(waitForButtonState);

        homingWaitForMovementState
            .whenFinished()
            .transitionTo(homingWaitForStopMovingState);

        homingWaitForMovementState
            .whenTimeout(JsonConstants.armConstants.homingTimeoutSeconds)
            .transitionTo(homingDoneState);

        homingWaitForStopMovingState
            .when(DriverStation::isDisabled, "When robot is disabled during homing")
            .transitionTo(waitForButtonState);
        
        homingWaitForStopMovingState
            .whenFinished()
            .transitionTo(homingDoneState);
        
        homingDoneState
            .whenFinished()
            .transitionTo(controlToPositionState);
        
        this.stateMachine.setState(waitForButtonState);
        StateMachineDump.write("arm", this.stateMachine);
    }

    /**
     * @param angle The angle the arm should be set to
     */
    public void setTargetArmAngle(Angle angle){
        this.targetArmAngle = angle;
        this.holdVoltage = null;
    }

    /**
     * @return The target angle the arm is currently set to
     */
    public Angle getTargetArmAngle(){
        return this.targetArmAngle;
    }

    /**
     * @return The current angle the arm is at in Radians
     */
    public Angle getCurrentArmAngle(){
        return Radians.of(this.armInputs.positionRadians);
    }

    /** Stow away the arm */
    public void stow(){
        //TODO: Implement Method
    }

    /** Deploy the arm */
    public void deploy(){
        //TODO: Implement Method
    }

    /** Switch Test Mode on if it is off and off if it is on */
    public void changeTestMode(){
        this.shouldBeInTestMode = !this.shouldBeInTestMode;
    }

    /**
     * @return a boolean of if we are in test mode
     */
    public boolean isInTestMode(){
        return this.shouldBeInTestMode;
    }

}
