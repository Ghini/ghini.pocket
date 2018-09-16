package me.ghini.pocket;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;
import de.timroes.base64.Base64;

import static me.ghini.pocket.MainActivity.deviceId;

public class DesktopClientActivity extends AppCompatActivity {

    static final String SERVER_IP_ADDRESS = "ServerIPAddress";
    static final String SERVER_PORT = "ServerPort";
    static final String USER_NAME = "UserName";
    static final String SECURITY_CODE = "SecurityCode";

    private Bundle state;

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putAll(this.state);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // basic initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop_client);

        // restore state, or initialize it
        this.state = new Bundle();
        if (savedInstanceState != null) {
            state.putAll(savedInstanceState);
        } else {
            state.putString(SERVER_IP_ADDRESS, "192.168.43.226");
            state.putString(SERVER_PORT, "44464");
            state.putString(USER_NAME, "");
            state.putString(SECURITY_CODE, "");
        }

        // initialize references to widgets
        EditText etServerIPAddress = findViewById(R.id.etServerIPAddress);
        EditText etServerPort = findViewById(R.id.etServerPort);
        EditText etUserName = findViewById(R.id.etUserName);
        EditText etSecurityCode = findViewById(R.id.etSecurityCode);

        // refresh widgets with state
        etServerIPAddress.setText(state.getString(SERVER_IP_ADDRESS, ""));
        etServerPort.setText(state.getString(SERVER_PORT, ""));
        etUserName.setText(state.getString(USER_NAME, ""));
        etSecurityCode.setText(state.getString(SECURITY_CODE, ""));

        // set up edit callbacks as AfterTextChangedWatcher
        etServerIPAddress.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable target) {
                    afterTextViewChanged(target, SERVER_IP_ADDRESS);
                }
        });
        etServerPort.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable target) {
                afterTextViewChanged(target, SERVER_PORT);
                }
        });
        etUserName.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable target) {
                afterTextViewChanged(target, USER_NAME);
                }
        });
        etSecurityCode.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable target) {
                afterTextViewChanged(target, SECURITY_CODE);
                }
        });
    }

    private void afterTextViewChanged(Editable target, String fieldName) {
        state.putString(fieldName, String.valueOf(target.toString()));
    }

    private String recomputeUrlText() {
        return "http://" + state.getString(SERVER_IP_ADDRESS) + ":" + state.get(SERVER_PORT) + "/API1";
    }

    public void onVerify(final View view) {
        final Activity activity = this;
        String urlText = recomputeUrlText();
        try {
            XMLRPCCallback listener = new XMLRPCCallback() {
                public void onResponse(long id, Object result) {
                    final Object o = result;
                    if(result instanceof Integer) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //If no user is registered for your phone, a notification will
                                //briefly flash on your phone, asking you to please register.
                                Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        final String userName = result.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((EditText) findViewById(R.id.etUserName)).setText(userName);
                            }
                        });
                    }
                }
                public void onError(long id, XMLRPCException error) {
                    final Object o = error;
                    // Handling any error in the library
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                public void onServerError(long id, XMLRPCServerException error) {
                    final Object o = error;
                    // Handling an error response from the server
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            XMLRPCClient client = new XMLRPCClient(new URL(urlText));
            client.callAsync(listener, "verify", deviceId);
        } catch (MalformedURLException e) {
            Toast.makeText(this, "Malformed URL", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Some Error " + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void onRegister(View view) {
        final Activity activity = this;
        String urlText = recomputeUrlText();
        try {
            XMLRPCCallback listener = new XMLRPCCallback() {
                public void onResponse(long id, Object result) {
                    final Object o = result;
                    runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                }
                public void onError(long id, XMLRPCException error) {
                    final Object o = error;
                    // Handling any error in the library
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                public void onServerError(long id, XMLRPCServerException error) {
                    final Object o = error;
                    // Handling an error response from the server
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            XMLRPCClient client = new XMLRPCClient(new URL(urlText));
            client.callAsync(listener, "register", deviceId, state.getString(USER_NAME), state.getString(SECURITY_CODE));
        } catch (MalformedURLException e) {
            Toast.makeText(this, "Malformed URL", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Some Error " + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void onPush(View view) {
        final Activity activity = this;
        String urlText = recomputeUrlText();
        try {
            XMLRPCCallback listener = new XMLRPCCallback() {
                public void onResponse(long id, Object result) {
                    final Object o = result;
                    if((Integer)result != 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                public void onError(long id, XMLRPCException error) {
                    final Exception e = error;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                public void onServerError(long id, XMLRPCServerException error) {
                    final Exception e = error;
                    runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                    });
                }
            };
            XMLRPCClient client = new XMLRPCClient(new URL(urlText), XMLRPCClient.FLAGS_8BYTE_INT);
            String logFileName = new File(getExternalFilesDir(null), "searches.txt").getAbsolutePath();
            Set<String> pictures = new HashSet<>();
            List<String> lines = new LinkedList<>();
            BufferedReader br = new BufferedReader(new FileReader(logFileName));
            // split lines and collect pictures
            for (String line = br.readLine(); line!=null; line=br.readLine()) {
                lines.add(line);
                // are there any pictures?
                Integer positionOfFirstFile = line.indexOf("file:///");
                if(positionOfFirstFile > 0) {
                    String[] names = line.substring(positionOfFirstFile).split(" : ");
                    pictures.addAll(Arrays.asList(names));
                }
            }
            File pocket = new File(activity.getExternalFilesDir(null), "pocket.db");
            long baseline = 0;  // with this baseline, server will reject all changes.
            if (pocket.exists()) {
                baseline = pocket.lastModified();
            }
            client.callAsync(listener, "put_change", deviceId, lines, baseline);
            for (String name:pictures) {
                File file = new File(name.substring(7));
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                //noinspection ResultOfMethodCallIgnored
                fis.read(data);
                fis.close();
                String content64 = Base64.encode(data);
                client.callAsync(listener, "put_picture", deviceId, file.getName(), content64);
            }
            br.close();
        } catch (MalformedURLException e) {
            Toast.makeText(this, "Malformed URL", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onPull(View view) {
        final Activity activity = this;
        String urlText = recomputeUrlText();
        try {
            XMLRPCCallback listener = new XMLRPCCallback() {
                public void onResponse(long id, Object result) {
                    final Object o = result;
                    if(result instanceof Integer) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        String filename = new File(activity.getExternalFilesDir(null), "pocket.db").getAbsolutePath();
                        try {
                            FileOutputStream fos = new FileOutputStream(filename);
                            byte[] decoded = Base64.decode(result.toString());
                            fos.write(decoded);
                            fos.close();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "OK", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            final Exception ee = e;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, ee.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
                public void onError(long id, XMLRPCException error) {
                    final Object o = error;
                    // Handling any error in the library
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                public void onServerError(long id, XMLRPCServerException error) {
                    final Object o = error;
                    // Handling an error response from the server
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, o.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            // This also resets the log, which gets anyway overruled by the new snapshot.  Since
            // this is a potentially destructive operation, you need to confirm you really mean it.
            XMLRPCClient client = new XMLRPCClient(new URL(urlText));
            client.callAsync(listener, "get_snapshot", deviceId);
        } catch (MalformedURLException e) {
            Toast.makeText(this, "Malformed URL", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Some Error " + e, Toast.LENGTH_SHORT).show();
        }
    }
}

