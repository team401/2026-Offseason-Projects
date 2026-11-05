package first.subsystems;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;

import coppercore.wpilib_interface.tuning.TestModeDescription;

public enum ArmTest implements TestModeDescription {
    // Arm-related test modes
    ArmVoltageTuning("Voltage"),
    ArmCurrentTuning("Current"),
    ArmClosedLoopTuning("Closed Loop"),
    ArmPhoenixTuning("Phoenix"),
    None;

    private final String description;

    ArmTestMode(String description) {
        this.description = description;
    }

    PivotTestMode(){
        this.description = name();
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
