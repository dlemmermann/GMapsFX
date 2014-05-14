/*
 * Copyright 2014 Lynden, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lynden.gmapsfx;

import com.lynden.gmapsfx.javascript.JavaFxWebEngine;
import com.lynden.gmapsfx.javascript.JavascriptRuntime;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 *
 * @author Rob Terpilowski
 */
public class GoogleMapView extends AnchorPane {

    protected WebView webview;
    protected JavaFxWebEngine webengine;
    protected boolean initialized = false;
    protected final CyclicBarrier barrier = new CyclicBarrier(2);
    protected final List<MapComponentInitializedListener> mapInitializedListeners = new ArrayList<>();
    protected GoogleMap map;


    public GoogleMapView() {
        webview = new WebView();
        webengine = new JavaFxWebEngine(webview.getEngine());
        JavascriptRuntime.setDefaultWebEngine( webengine );

        setTopAnchor(webview,0.0);
        setLeftAnchor(webview,0.0);
        setBottomAnchor(webview, 0.0);
        setRightAnchor(webview, 0.0);
        getChildren().add(webview);
        
        webengine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED) {
                            setInitialized(true);
//                            webview.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
//                                @Override
//                                public void handle(WebEvent<String> event) {
//                                    System.out.println("Alert: " + event.getData());
//                                }
//                            });
//                            //webengine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
//                            JSObject jsobj = (JSObject) webengine.executeScript("window");                      
//                            jsobj.setMember("java", new JSListener());
                            fireMapInitializedListeners();
                        }
                    }
                });
        webengine.load(getClass().getResource("/html/maps.html").toExternalForm());
        
    }

    public void setZoom(int zoom) {
        checkInitialized();
        map.setZoom(zoom);
    }

    public void setCenter(double latitude, double longitude) {
        checkInitialized();
        LatLong latLong = new LatLong(latitude, longitude);
        map.setCenter(latLong);
    }

    public GoogleMap getMap() {
        checkInitialized();
        return map;
    }

    public GoogleMap createMap( MapOptions mapOptions ) {
        checkInitialized();
        map = new GoogleMap(mapOptions);
        return map;
    }

    public GoogleMap createMap() {
        map = new GoogleMap();
        return map;
    }

    public void addMapInializedListener(MapComponentInitializedListener listener) {
        synchronized (mapInitializedListeners) {
            mapInitializedListeners.add(listener);
        }
    }

    public void removeMapInitializedListener(MapComponentInitializedListener listener) {
        synchronized (mapInitializedListeners) {
            mapInitializedListeners.remove(listener);
        }
    }

    protected void init() {

    }

    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    protected void fireMapInitializedListeners() {
        synchronized (mapInitializedListeners) {
            for (MapComponentInitializedListener listener : mapInitializedListeners) {
                listener.mapInitialized();
            }
        }
    }

    protected JSObject executeJavascript(String function) {
        Object returnObject = webengine.executeScript(function);
        return (JSObject) returnObject;
    }

    protected String getJavascriptMethod(String methodName, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName).append("(");
        for (Object arg : args) {
            sb.append(arg).append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");

        return sb.toString();
    }

    protected void checkInitialized() {
        if (!initialized) {
            throw new MapNotInitializedException();
        }
    }
    
    public class JSListener { 
        public void log(String text){
            System.out.println(text);
        }
    }
    
}