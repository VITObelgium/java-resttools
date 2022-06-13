package be.vito.rma.resttools.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author (c) 2016 Stijn.VanLooy@vito.be
 *
 */
public class JsonTools {

	private static ObjectMapper mapper = new ObjectMapper();

	static {
		// ignore properties we do not know about
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// don't serialize null values
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	/**
	 * Register a module to the underlying ObjectMapper
	 * For example, for adding providers for custom serializers and deserializers.
	 * @param module
	 */
	public static void registerModule (final Module module) {
		mapper.registerModule(module);
	}

	public static String prettifyJson (final String json) throws IOException {
		// generate pretty output
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		final Object jsonObject = mapper.readValue(json, Object.class);
		return mapper.writeValueAsString(jsonObject);
	}

	public static <T> String toPrettyJson (final T data) {
		// generate pretty output
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		return toJson(data);
	}

	public static <T> void toPrettyJson (final OutputStream os, final T data) {
		// generate pretty output
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		toJson(os, data);

	}

	public static <T> String toCompactJson (final T data) {
		// generate compact output
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		return toJson(data);
	}

	public static <T> void toCompactJson (final OutputStream os, final T data) {
		// generate pretty output
		mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		toJson(os, data);
	}

	private static <T> String toJson (final T data) {
		try {
			return mapper.writeValueAsString(data);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException("Error while serializing " + data.getClass().getName()
					+ " into JSON.", e);
		}
	}

	private static <T> void toJson (final OutputStream os, final T data) {
		try {
			mapper.writeValue(os, data);
		} catch (final IOException e) {
			throw new RuntimeException("Error while serializing " + data.getClass().getName()
					+ " into JSON.", e);
		}
	}

	public static <T> T fromJson (final String json, final Class<T> type) {
		try {
			return mapper.readValue(json, type);
		} catch (final IOException e) {
			throw new RuntimeException("Error while deserializing JSON into "
					+ type.getName() + ": " + json, e);
		}
	}

	public static <T> T fromJson (final String json, final TypeReference<T> type) {
		try {
			return mapper.readValue(json, type);
		} catch (final IOException e) {
			throw new RuntimeException("Error while deserializing JSON into "
					+ type.getType().getTypeName() + ": " + json, e);
		}
	}

	public static <T> T fromJson (final InputStream json, final Class<T> type) {
		try {
			return mapper.readValue(json, type);
		} catch (final IOException e) {
			throw new RuntimeException("Error while deserializing JSON into "
					+ type.getName() + " from input stream", e);
		}
	}

	public static <T> T fromJson (final InputStream json, final TypeReference<T> type) {
		try {
			return mapper.readValue(json, type);
		} catch (final IOException e) {
			throw new RuntimeException("Error while deserializing JSON into "
					+ type.getType().getTypeName() + " from input stream", e);
		}
	}

	public static <T> T map2pojo (final Map<String, Object> map, final Class<T> pojo) {
		return mapper.convertValue(map, pojo);
	}

}

