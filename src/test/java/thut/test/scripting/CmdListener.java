package thut.test.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import thut.test.scripting.handlers.CmdHandler;
import thut.test.scripting.handlers.GetBlockHandler;
import thut.test.scripting.handlers.GetPlayerHandler;
import thut.test.scripting.handlers.SetBlockHandler;

public class CmdListener
{

    public static class CmdListenServer
    {
        private MinecraftServer server;

        public List<ICmdHandler> handlers = Lists.newArrayList();

        private ServerSocket   serverSocket;
        private Socket         clientSocket;
        private PrintWriter    out;
        private BufferedReader in;

        public CmdListenServer()
        {
            this.handlers.add(new GetBlockHandler());
            this.handlers.add(new SetBlockHandler());
            this.handlers.add(new GetPlayerHandler());
            this.handlers.add(new CmdHandler());
        }

        public void start(final int port) throws IOException
        {
            // Seems we had already started, but not stopped, lets make sure
            // this is stopped.
            if (this.out != null) this.stop();
            this.serverSocket = new ServerSocket(port);
            this.clientSocket = this.serverSocket.accept();
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        }

        public boolean read() throws IOException
        {
            while (!this.in.ready())
                try
                {
                    Thread.sleep(0, 1000);
                }
                catch (final InterruptedException e)
                {
                    return false;
                }
            final AtomicBoolean did = new AtomicBoolean(false);
            final AtomicBoolean done = new AtomicBoolean(true);
            final StringBuilder textBuilder = new StringBuilder();
            int c = 0;
            while ((c = this.in.read()) != -1 && this.in.ready())
                textBuilder.append((char) c);
            final String msg = textBuilder.toString().trim();
            done.set(msg.length() == 0);
            this.server.execute(() ->
            {
                for (final ICmdHandler handler : this.handlers)
                {
                    String ret;
                    try
                    {
                        ret = handler.handle(this.server, msg);
                    }
                    catch (final Exception e)
                    {
                        continue;
                    }
                    if (ret != null)
                    {
                        this.out.printf(ret);
                        did.set(true);
                        break;
                    }
                }
                done.set(true);
            });

            while (!done.get())
                try
                {
                    Thread.sleep(0, 100);
                }
                catch (final InterruptedException e)
                {
                    return false;
                }
            return did.get();
        }

        public void stop() throws IOException
        {
            // Never started in the first place, handle this gracefully.
            if (this.out == null) return;
            this.in.close();
            this.out.close();
            this.clientSocket.close();
            this.serverSocket.close();
            this.in = null;
            this.out = null;
            this.clientSocket = null;
            this.serverSocket = null;
        }

        public MinecraftServer getServer()
        {
            return this.server;
        }

        public void setServer(final MinecraftServer server)
        {
            this.server = server;
        }
    }

    static final CmdListenServer server = new CmdListenServer();

    static int port = 6666;

    static Thread listener;

    static Thread makeListener()
    {
        return new Thread(() ->
        {
            while (CmdListener.server.server != null)
                try
                {
                    try
                    {
                        CmdListener.server.start(CmdListener.port);
                    }
                    catch (final IOException e)
                    {

                    }
                    catch (final Exception e)
                    {
                        System.err.println("Error with port listener, quitting here.");
                        e.printStackTrace();
                        return;
                    }
                    CmdListener.server.read();
                    CmdListener.server.stop();
                }
                catch (final IOException e)
                {
                    e.printStackTrace();
                }
        });
    }

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(CmdListener::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(CmdListener::onServerStopped);
    }

    private static void onServerStarting(final FMLServerStartingEvent event)
    {
        CmdListener.server.setServer(event.getServer());
        CmdListener.listener = CmdListener.makeListener();
        CmdListener.listener.setName("CmdListening");
        CmdListener.listener.start();
    }

    private static void onServerStopped(final FMLServerStoppedEvent event)
    {
        CmdListener.server.setServer(null);
        CmdListener.listener.interrupt();
        try
        {
            CmdListener.server.stop();
        }
        catch (final IOException e)
        {
        }
    }
}
