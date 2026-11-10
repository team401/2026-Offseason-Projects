package first.robot.constants;

import static org.wpilib.units.Units.RadiansPerSecond;
import static org.wpilib.units.Units.Volts;

import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Voltage;

public class ElevatorConstants {
  public Double elevatorKP = 0.0;
  public Double elevatorKI = 0.0;
  public Double elevatorKD = 0.0;
  public Double elevatorKS = 0.0;
  public Double elevatorKG = 0.0;
  public Double elevatorKV = 0.0;
  public Double elevatorKA = 0.0;

  public Double elevatorExpoKV = 0.0;
  public Double elevatorExpoKA = 0.0;

  public Voltage homingVoltage = Volts.of(0.0);
  public final AngularVelocity homingMovementThreshold = RadiansPerSecond.of(2.0);
}
