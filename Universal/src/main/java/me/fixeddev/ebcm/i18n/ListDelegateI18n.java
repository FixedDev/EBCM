package me.fixeddev.ebcm.i18n;

import me.fixeddev.ebcm.NamespaceAccesor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListDelegateI18n implements I18n {

    private final List<I18n> delegates;

    public ListDelegateI18n(List<I18n> delegates) {
        this.delegates = new CopyOnWriteArrayList<>(delegates);
    }

    public ListDelegateI18n() {
        delegates = new CopyOnWriteArrayList<>();
    }

    @Override
    public String getMessage(String messageId, NamespaceAccesor namespace) {
        for (I18n delegate : delegates) {
            String message = delegate.getMessage(messageId, namespace);

            if (message != null) {
                return message;
            }
        }

        return null;
    }

    public void registerDelegate(I18n i18n) {
        if (delegates.contains(i18n)) {
            return;
        }

        delegates.add(0, i18n);
    }

    public void removeDelegate(I18n i18n) {
        delegates.remove(i18n);
    }
}
