package thut.tech.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public final class LogFormatter extends Formatter
{
    private static final String SEP = System.getProperty("line.separator");

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(this.dateFormat.format(record.getMillis()));
        sb.append(" [").append(record.getLevel().getLocalizedName()).append("] ");

        sb.append(record.getMessage());
        sb.append(LogFormatter.SEP);
        final Throwable thr = record.getThrown();

        if (thr != null)
        {
            final StringWriter thrDump = new StringWriter();
            thr.printStackTrace(new PrintWriter(thrDump));
            sb.append(thrDump.toString());
        }

        return sb.toString();
    }
}
