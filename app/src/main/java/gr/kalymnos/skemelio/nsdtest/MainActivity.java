package gr.kalymnos.skemelio.nsdtest;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class MainActivity extends AppCompatActivity {
    private static final String SERVICE_TYPE = "_localdash._tcp";
    private static final String TRAILING_DOT = ".";

    private ServerSocket serverSocket;
    private int localPort;

    private NsdManager.RegistrationListener registrationListener;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager nsdManager;
    private String serviceName;
    private NsdServiceInfo service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeRegistrationListener();
        initializeServerSocket();
        nsdManager = (NsdManager) getSystemService(NSD_SERVICE);
    }

    public void onRegisterClicked(View view) {
        registerService(localPort);
    }

    public void onDiscoverClicked(View view) {
        if (discoveryListener == null)
            initializeDiscoveryListener();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void onConnectClicked(View view) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nsdManager != null)
            nsdManager.unregisterService(registrationListener);
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("localdash");
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public void initializeServerSocket() {
        try {
            serverSocket = new ServerSocket(0);
            localPort = serverSocket.getLocalPort();
        } catch (IOException e) {
            Toast.makeText(this, "Error with server socket", Toast.LENGTH_SHORT).show();
        }
    }

    public void initializeDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String s, int i) {
                Toast.makeText(MainActivity.this, "Start discovery failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopDiscoveryFailed(String s, int i) {
                Toast.makeText(MainActivity.this, "Stop discovery failed ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStarted(String s) {
                Toast.makeText(MainActivity.this, "Started discovery", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStopped(String s) {
                Toast.makeText(MainActivity.this, "Stopped discovery", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                Toast.makeText(MainActivity.this, nsdServiceInfo.getServiceName() + " service found", Toast.LENGTH_SHORT).show();

                if (!nsdServiceInfo.getServiceType().equals(SERVICE_TYPE+TRAILING_DOT)) {
                    Toast.makeText(MainActivity.this, "Unknown service type "+nsdServiceInfo.getServiceType(), Toast.LENGTH_SHORT).show();
                } else if (nsdServiceInfo.getServiceName().equals(serviceName)) {
                    // The one who registered the service is the one which actually holds the
                    // service name and its not null, so if we are here its the same machine
                    // who registered the service in the first place.
                    Toast.makeText(MainActivity.this, "Same machine: " + serviceName, Toast.LENGTH_SHORT).show();
                } else if (nsdServiceInfo.getServiceName().contains("localdash")) {
                    nsdManager.resolveService(nsdServiceInfo, new NsdManager.ResolveListener() {
                        @Override
                        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                            Toast.makeText(MainActivity.this, "Could not resolve " + nsdServiceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                            Toast.makeText(MainActivity.this, "Resolved  " + nsdServiceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
                            if (nsdServiceInfo.getServiceName().equals(serviceName)){
                                Toast.makeText(MainActivity.this, "Same IP", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            service = nsdServiceInfo;
                            int port = service.getPort();
                            InetAddress host = service.getHost();
                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                Toast.makeText(MainActivity.this, nsdServiceInfo.getServiceName() + " service lost", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Toast.makeText(MainActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Toast.makeText(MainActivity.this, "Unregistration failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                Toast.makeText(MainActivity.this, "Service registered", Toast.LENGTH_SHORT).show();
                serviceName = nsdServiceInfo.getServiceName();
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
                Toast.makeText(MainActivity.this, "Service unregistered", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
