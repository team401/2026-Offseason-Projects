package first.subsystems;

import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.Voltage;
import coppercore.wpilib_interface.MonitoredSubsystem;

public class ArmSubsystem extends MonitoredSubsystem{
    
    TestModeManager<ArmTestMode> armTestModeManager = new TestModeManager<ArmTestMode>("Arm", ArmTestMode.class);

    StateMachine<ArmSubsystem> armStateMachine;

    protected MotorIO armMotorIO;

    protected MotorInputsAutoLogged armInputs;

    protected Angle targetArmAngle = Degrees.zero();
    protected Voltage holdVoltage = null; 

    private ArmDependencies dependencies = new ArmDependencies();

    public static class ArmDependencies {
        private boolean isHomingSwitchPressed = false;

        /**
         * @return A boolean representing whether or not the homing switch is pressed
         */
        public boolean isHomingSwitchPressed(){
            return isHomingSwitchPressed;
        }
    }

    /**
     * @return Dependencies for Arm Subsystem
     */
    public ArmDependencies getDependencies() {
        return this.dependencies;
    }

    /**
     * Setter for whether or not the homing switch is pressed
     * @param isHomingSwitchPressed boolean for the pressed condition of the homing switch
     */
    public void setIsHomingSwitchPressed(boolean isHomingSwitchPressed){
        dependencies.isHomingSwitchPressed = isHomingSwitchPressed;
    }

    /**
     * Constructor for the ArmSubsytem Class
     * @param armMotorIO The arms generic motor IO
     */
    public ArmSubsystem(MotorIO armMotorIO){}

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
    public void getTargetArmAngle(){
        return this.targetArmAngle;
    }

    /**
     * @return The current angle the arm is at in Radians
     */
    public void getCurrentArmAngle(){
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


}
