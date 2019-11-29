package se._1177.lmn.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import se._1177.lmn.service.util.Constants;

import java.io.Serializable;
import java.util.Objects;
import java.util.Stack;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NavigationController implements Serializable {

    private Stack<View> views = new Stack<>();

    public void init(String value, String label) {
        views.clear();
        views.push(View.from(value, label));
    }

    public String gotoView(String view, String name) {
        return views.push(View.from(view, name)).value;
    }

    public String goBack() {
        views.pop();
        return views.peek().value;
    }

    public Stack<View> getViews() {
        return views;
    }

    public boolean hasVisitedCollectDelivery() {
        return views.stream().map(v -> v.label).anyMatch(value -> value.equals(CollectDeliveryController.VIEW_NAME));
    }

    public String goBackTo(String view, String name) {
        return goBackTo(View.from(view, name));
    }

    public String goBackTo(View view) {
        while (!views.peek().equals(view)) {
            views.pop();
        }
        return views.peek().value;
    }

    public String getActionSuffix() {
        return Constants.ACTION_SUFFIX;
    }

    public void ensureLastViewIs(String viewWithoutSuffix, String name) {

        View view = View.from(viewWithoutSuffix + Constants.ACTION_SUFFIX, name);

        int index = views.indexOf(view);

        if (index == views.size() - 1) {
            // Do nothing. It is already last.
            return;
        }

        if (index > -1) {
            goBackTo(view);
        } else {
            // The user has probably used the browser back button to jump forward in the flow
            gotoView(view.value, view.label);
        }
    }

    public static class View implements Serializable {
        String value;
        String label;

        private View(String value, String label) {
            this.value = value;
            this.label = label;
        }

        static View from(String value, String label) {
            return new View(value, label);
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            View view = (View) o;
            return Objects.equals(value, view.value) &&
                    Objects.equals(label, view.label);
        }

        @Override
        public int hashCode() {

            return Objects.hash(value, label);
        }
    }
}
