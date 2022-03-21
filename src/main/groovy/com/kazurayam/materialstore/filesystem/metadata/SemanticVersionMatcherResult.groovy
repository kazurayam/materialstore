package com.kazurayam.materialstore.filesystem.metadata

import com.kazurayam.materialstore.filesystem.Jsonifiable
import com.kazurayam.materialstore.util.JsonUtil

import java.util.regex.Matcher

class SemanticVersionMatcherResult implements Jsonifiable {

    private boolean matched
    private List<String> fragments

    SemanticVersionMatcherResult(Matcher m) {
        this.matched = m.matches()
        fragments = new ArrayList<>()
        if (m.matches()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                fragments.add(m.group(i))
            }
        }
    }

    boolean matched() {
        return matched
    }

    List<String> fragments() {
        return fragments
    }

    int size() {
        return fragments.size()
    }

    String getHeader() {
        return fragments.get(0)
    }

    String getTrailer() {
        return fragments.get(3)
    }

    String getVersion() {
        return fragments.get(1)
    }

    @Override
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"matched\":")
        sb.append(matched)
        sb.append(",")
        sb.append("\"fragments\":")
        sb.append("[")
        int count = 0
        for (String fragment : fragments) {
            if (count > 0) {
                sb.append(", ")
            }
            sb.append("\"")
            if (fragment != null) {
                sb.append(JsonUtil.escapeAsJsonString(fragment))
            }
            sb.append("\"")
            count += 1
        }
        sb.append("]")
        sb.append("}")
        return sb.toString()
    }

    @Override
    String toJson(boolean prettyPrint) {
        if (prettyPrint) {
            return JsonUtil.prettyPrint(toJson())
        } else {
            return toJson()
        }
    }
}
