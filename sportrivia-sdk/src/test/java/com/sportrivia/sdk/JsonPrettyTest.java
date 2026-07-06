package com.sportrivia.sdk;

import com.sportrivia.sdk.internal.services.JsonPretty;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonPrettyTest {

    @Test
    public void indentsOneFieldPerLinePreservingOrder() {
        String pretty = JsonPretty.indent("{\"zebra\":1,\"alpha\":2}");
        assertEquals("{\n  \"zebra\": 1,\n  \"alpha\": 2\n}", pretty);
    }

    @Test
    public void nestsAndKeepsEmptyContainersInline() {
        String pretty = JsonPretty.indent("{\"obj\":{\"k\":\"v\"},\"e\":{},\"list\":[1,2],\"el\":[]}");
        String expected = "{\n"
                + "  \"obj\": {\n"
                + "    \"k\": \"v\"\n"
                + "  },\n"
                + "  \"e\": {},\n"
                + "  \"list\": [\n"
                + "    1,\n"
                + "    2\n"
                + "  ],\n"
                + "  \"el\": []\n"
                + "}";
        assertEquals(expected, pretty);
    }

    @Test
    public void doesNotBreakOnBracesAndCommasInsideStrings() throws Exception {
        // Braces/commas/colons inside string values must be left untouched.
        String compact = "{\"a\":\"x,y:{}[]z\",\"b\":\"has \\\" quote\"}";
        String pretty = JsonPretty.indent(compact);
        JSONObject parsed = new JSONObject(pretty);
        assertEquals("x,y:{}[]z", parsed.getString("a"));
        assertEquals("has \" quote", parsed.getString("b"));
    }

    @Test
    public void outputReparsesToSameDocument() throws Exception {
        String compact = "{\"n\":2,\"arr\":[{\"x\":null},{\"y\":\"z\"}],\"s\":\"emoji 🎉\"}";
        String pretty = JsonPretty.indent(compact);
        assertTrue(pretty.contains("\n"));
        JSONObject parsed = new JSONObject(pretty);
        assertEquals(2, parsed.getInt("n"));
        assertEquals("emoji 🎉", parsed.getString("s"));
        assertEquals(2, parsed.getJSONArray("arr").length());
    }
}
