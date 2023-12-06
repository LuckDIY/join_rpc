package com.example.join_rpc.common.serialize;

import com.example.join_rpc.common.model.RpcRequest;
import com.example.join_rpc.common.model.RpcResponse;
import com.google.common.base.Charsets;
import com.google.gson.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

public class JsonMessageSerialize implements MessageSerialize {

    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(Class.class, new ClassTypeAdapter())
            .registerTypeAdapter(Exception.class, new ExceptionTypeAdapter())
            .create();

    public JsonMessageSerialize() {

    }

    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        return gson.toJson(request).getBytes(Charsets.UTF_8);
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) throws Exception {
        return gson.fromJson(new String(data, Charsets.UTF_8), RpcRequest.class);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return gson.toJson(response).getBytes(Charsets.UTF_8);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) throws Exception {
        return gson.fromJson(new String(data, Charsets.UTF_8), RpcResponse.class);
    }


    static class ClassTypeAdapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
        @Override
        public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {

            return new JsonPrimitive(src.getName());
        }

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                String className = json.getAsJsonPrimitive().getAsString();
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class ExceptionTypeAdapter implements JsonSerializer<Exception>, JsonDeserializer<Exception> {
        @Override
        public Exception deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            try {
                JsonObject asJsonObject = json.getAsJsonObject();
                String message = asJsonObject.get("message").getAsString();
                String className = asJsonObject.get("className").getAsString();
                Class<?> aClass = Class.forName(className);
                Constructor<?> constructor = aClass.getConstructor(String.class);
                return (Exception) constructor.newInstance(message);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public JsonElement serialize(Exception src, Type typeOfSrc, JsonSerializationContext context) {
            String message = src.getMessage();
            String name = src.getClass().getName();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", message);
            jsonObject.addProperty("className", name);
            return jsonObject;
        }
    }

}
