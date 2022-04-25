package com.bethibande.web.regex;

import com.bethibande.web.struct.URIFieldType;
import com.bethibande.web.handlers.FieldHandle;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatcher {

    //\{(.+):(string|number|dec)(\((\d+,\d+)\))?}
    public static String uriField = "\\{([a-zA-Z0-9-_]+):(string|text|number|num|dec|decimal|bool|boolean)(\\((\\d+,\\d+)\\))?}";
    public static String textUriField = "\\{(.+)\\:(string|text)(\\((\\d+,\\d+)\\))?\\}";
    public static String numUriField = "\\{(.+)\\:(number|num)(\\((\\d+,\\d+)\\))?\\}";
    public static String decUriField = "\\{(.+)\\:(dec|decimal)(\\((\\d+,\\d+)\\))?\\}";
    public static String boolUriField = "\\{(.+)\\:(boolean|bool)(\\((\\d+,\\d+)\\))?\\}";
    public static Pattern uriFieldPattern = Pattern.compile(RegexMatcher.uriField);

    /**
     * Turns uri specified by the user, into a string that will match this uri: /test/{name:string(4,16)}/{id:number} -> /test/.{4,16}/-?\d+
     * @param uri the user specified uri
     * @return the new regex uri
     */
    public static String rawUriToRegex(String uri) {

        Matcher m = uriFieldPattern.matcher(uri);
        while(m.find()) {
            String full = m.group(0);
            //String id = m.group(1);
            String type = m.group(2);
            String len = m.group(3);
            if(len != null) len = len.substring(1, len.length()-1);

            if(type.matches("string|text")) {
                //uri = uri.replace(full, "[a-zA-z0-9-_]" + (len != null ? "{"+len+"}": "+"));
                uri = uri.replace(full, "[^/]" + (len != null ? "{"+len+"}": "+"));
            }
            if(type.matches("number|num")) {
                uri = uri.replace(full, "[-+]?(\\d)" + (len != null ? "{"+len+"}": "+"));
            }
            if(type.matches("dec|decimal")) {
                uri = uri.replace(full, "[-+]?[0-9]*\\\\.?[0-9]+([eE][-+]?[0-9]+)?");
            }
            if(type.matches("bool|boolean")) {
                uri = uri.replace(full, "(true|false)");
            }
        }
        return uri;
    }

    public static Integer[] getIndexes(String uri) {
        List<Integer> indexes = new ArrayList<>();
        int i = 0;

        for(String s : uri.split("/")) {
            if(s == null) continue;
            if(s.matches(uriField)) indexes.add(i);
            i++;
        }

        return indexes.toArray(Integer[]::new);
    }

    public static Map<Integer, FieldHandle> getUriFields(String uri, Integer[] indexes) {
        String[] split = uri.split("/");
        HashMap<Integer, FieldHandle> handles = new HashMap<>();

        for(int i : indexes) {
            String field = split[i];
            Matcher m = uriFieldPattern.matcher(field);
            if(!m.find()) continue;

            String full = m.group(0);
            String id = m.group(1);
            String type = m.group(2);
            String len = m.group(3);
            URIFieldType fieldType;
            if(len != null) len = len.substring(1, len.length()-1);

            switch (type) {
                case "string":
                    fieldType = URIFieldType.STRING;
                    break;
                case "number": case "num":
                    fieldType = URIFieldType.NUMBER;
                    break;
                case "dec": case "decimal":
                    fieldType = URIFieldType.DECIMAL;
                    break;
                case "bool": case "boolean":
                    fieldType = URIFieldType.BOOLEAN;
                    break;
                default:
                    System.err.println("[JWebAPI] Invalid uri field type: '" + type + "' in uri " + uri);
                    fieldType = null;
            }

            handles.put(i, new FieldHandle(id, fieldType, len, i));
        }
        return handles;
    }

}
