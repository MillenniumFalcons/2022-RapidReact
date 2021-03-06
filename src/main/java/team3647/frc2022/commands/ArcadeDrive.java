package team3647.frc2022.commands;

import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj2.command.CommandBase;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import team3647.frc2022.subsystems.Drivetrain;

public class ArcadeDrive extends CommandBase {

    private final Drivetrain m_drivetrain;
    private final DoubleSupplier m_throttle;
    private final DoubleSupplier m_turn;
    private final BooleanSupplier m_quickTurn;
    private final SlewRateLimiter m_accelerationLimiter = new SlewRateLimiter(2);

    private final double driveMultiplier = 1;

    /** Creates a new ArcadeDrive. */
    public ArcadeDrive(
            Drivetrain drivetrain,
            DoubleSupplier throttle,
            DoubleSupplier turn,
            BooleanSupplier quickTurn) {
        // Use addRequirements() here to declare subsystem dependencies.
        m_drivetrain = drivetrain;
        m_throttle = throttle;
        m_turn = turn;
        m_quickTurn = quickTurn;
        addRequirements(drivetrain);
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {}

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {
        double throttle = m_throttle.getAsDouble() * driveMultiplier;
        double turn = m_turn.getAsDouble() * driveMultiplier;
        m_drivetrain.curvatureDrive(
                m_accelerationLimiter.calculate(throttle * throttle * Math.signum(throttle)),
                turn * turn * Math.signum(turn) * 0.8,
                m_quickTurn.getAsBoolean());
    }

    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {}

    // Returns true when the command should end.
    @Override
    public boolean isFinished() {
        return false;
    }
}
