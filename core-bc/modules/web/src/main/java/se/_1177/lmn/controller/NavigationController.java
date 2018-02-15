package se._1177.lmn.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Stack;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class NavigationController {

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

    public String goBackTo(View view) {
        while (!views.peek().equals(view)) {
            views.pop();
        }
        return views.peek().value;
    }

    public static class View {
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
