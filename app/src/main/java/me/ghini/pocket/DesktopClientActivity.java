package me.ghini.pocket;
/*
  This file is part of ghini.pocket.

  ghini.pocket is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option)
  any later version.

  ghini.pocket is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  for more details.

  You should have received a copy of the GNU General Public License along
  with ghini.pocket.  If not, see <http://www.gnu.org/licenses/>.

  Copyright © 2018 Mario Frasca. <mario@anche.no>
  Copyright © 2018 Tanager Botanical Garden. <tanagertourism@gmail.com>

  Created by mario on 2018-09-03.
*/

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;
import de.timroes.base64.Base64;

import static me.ghini.pocket.MainActivity.deviceId;
import static me.ghini.pocket.MainActivity.resources;
import static me.ghini.pocket.MainActivity.serverIP;
import static me.ghini.pocket.MainActivity.serverPort;

@SuppressWarnings("RedundantCast")
public class DesktopClientActivity extends AppCompatActivity {

    static final String SERVER_IP_ADDRESS = "ServerIPAddress";
    static final String SERVER_PORT = "ServerPort";
    static final String USER_NAME = "UserName";
    static final String SECURITY_CODE = "SecurityCode";
    private Map<Integer,String> errorString = new HashMap<Integer, String>(){
        {
            put(-1, resources.getString(R.string.generic_error));
            put(0, resources.getString(R.string.OK));
            put(1, resources.getString(R.string.unregistered_user));
            put(2, resources.getString(R.string.wrong_types));
            put(3, resources.getString(R.string.invalid_code));
            put(4, resources.getString(R.string.file_exists));
            put(5, resources.getString(R.string.server_busy_please_wait));
            put(16, resources.getString(R.string.user_already_registered));
        }
    };

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
            state.putString(SERVER_IP_ADDRESS, serverIP);
            state.putString(SERVER_PORT, serverPort);
            state.putString(USER_NAME, "");
            state.putString(SECURITY_CODE, "");
        }

        // initialize references to widgets
        EditText etServerIPAddress = (EditText) findViewById(R.id.etServerIPAddress);
        EditText etServerPort = (EditText) findViewById(R.id.etServerPort);
        EditText etUserName = (EditText) findViewById(R.id.etUserName);
        EditText etSecurityCode = (EditText) findViewById(R.id.etSecurityCode);

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
                serverIP = String.valueOf(target.toString());
            }
        });
        etServerPort.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable target) {
                afterTextViewChanged(target, SERVER_PORT);
                serverPort = String.valueOf(target.toString());
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
                    if(result instanceof Integer) {
                        final Integer o = (Integer) result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //If no user is registered for your phone, a notification will
                                //briefly flash on your phone, asking you to please register.
                                Toast.makeText(activity, errorString.get(o), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onRegister(View view) {
        final Activity activity = this;
        String urlText = recomputeUrlText();
        try {
            XMLRPCCallback listener = new XMLRPCCallback() {
                public void onResponse(long id, Object result) {
                    final Integer o = (Integer) result;
                    runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, errorString.get(o), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onPush(View view) {
        final Activity activity = this;
        String urlText = recomputeUrlText();
        try {
            XMLRPCCallback listener = new XMLRPCCallback() {
                public void onResponse(long id, Object result) {
                    final Integer o = (Integer) result;
                    if((Integer)result != 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, errorString.get(o), Toast.LENGTH_SHORT).show();
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
                    if(result instanceof Integer) {
                        final Integer o = (Integer) result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, errorString.get(o), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

