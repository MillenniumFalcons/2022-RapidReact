package team3647.frc2022.commands.turret;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import java.util.function.DoubleSupplier;
import team3647.frc2022.subsystems.Turret;

public class TurretCommands {
    private final Turret m_turret;

    public TurretCommands(Turret m_turret) {
        this.m_turret = m_turret;
    }

    public Command getTurretManual(DoubleSupplier output) {
        return new RunCommand(() -> m_turret.setOpenloop(output.getAsDouble()), m_turret);
    }

    public Command getTurretMotionMagic(double angle) {
        return new FunctionalCommand(
                () -> {},
                () -> m_turret.setAngleMotionMagic(angle),
                interrupted -> {},
                () -> Math.abs(m_turret.getAngle() - angle) < 1,
                m_turret);
    }
}
