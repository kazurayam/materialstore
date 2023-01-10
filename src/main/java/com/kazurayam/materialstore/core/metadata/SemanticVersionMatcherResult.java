package com.kazurayam.materialstore.core.metadata;

import com.kazurayam.materialstore.core.Jsonifiable;
import com.kazurayam.materialstore.core.TemplateReady;
import com.kazurayam.materialstore.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public final class SemanticVersionMatcherResult implements Jsonifiable, TemplateReady {

    private boolean matched;
    private List<String> fragments;

    public SemanticVersionMatcherResult(Matcher m) {
        this.matched = m.matches();
        fragments = new ArrayList<String>();
        if (m.matches()) {
            for (int i = 1; i <= m.groupCount() ; i++){
                fragments.add(m.group(i));
            }
        }
    }

    public boolean matched() {
        return matched;
    }

    public List<String> fragments() {
        return fragments;
    }

    public int size() {
        return fragments.size();
    }

    public String getHeader() {
        return fragments.get(0);
    }

    public String getTrailer() {
        return fragments.get(3);
    }

    public String getVersion() {
        return fragments.get(1);
    }

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"matched\":");
        sb.append(matched);
        sb.append(",");
        sb.append("\"fragments\":");
        sb.append("[");
        int count = 0;
        for (String fragment : fragments) {
            if (count > 0) {
                sb.append(", ");
            }
            sb.append("\"");
            if (fragment != null) {
                sb.append(JsonUtil.escapeAsJsonString(fragment));
            }
            sb.append("\"");
            count += 1;
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson());
        } else {
            return toJson();
        }
    }

}
