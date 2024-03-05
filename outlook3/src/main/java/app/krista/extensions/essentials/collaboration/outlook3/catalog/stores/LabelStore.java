package app.krista.extensions.essentials.collaboration.outlook3.catalog.stores;

import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extension.util.EntityStore;
import app.krista.extensions.essentials.collaboration.outlook3.catalog.entities.Label;
import app.krista.extensions.essentials.collaboration.outlook3.impl.AccountImpl;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.outlook3.service.Account;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * To search for labels in Outlook mail
 */
@Service
public class LabelStore implements EntityStore<Label> {

    private final Account account;

    @Inject
    private LabelStore(AccountImpl account) {
        this.account = account;
    }

    @Override
    public Label create(Map<String, Object> map) {
        return null;
    }

    @Override
    public Label get(String primaryKey) {
        return Label.fromField(account.getFolderByName(primaryKey, null, null).getFolderName());
    }

    @Override
    public Label update(Label labels) {
        return null;
    }

    @Override
    public void delete(String s) {
        // Delete entity feature is not supported
    }

    @Override
    public boolean contains(String s) {
        return false;
    }

    @SuppressWarnings(value = "deprecation")
    @Override
    public List<Label> search(List<SearchCondition> conditions, long pageIndex, int pageSize) {
        return Collections.emptyList();
    }

    @Override
    public List<Label> search(SearchQuery searchQuery, long l, int i) throws IOException {
        final List<SearchCondition> searchConditions = searchQuery.getSearchConditions();
        if (searchConditions == null || searchConditions.isEmpty()) {
            throw new IllegalArgumentException(Constants.FAILED_TO_SEARCH_FOR_LABELS + Constants.SEARCH_CONDITIONS_NOT_FOUND);
        }
        if (searchConditions.size() == 1) {
            return account.getFolderNames().stream().map(Label::fromField).collect(Collectors.toList());
        }
        List<Label> resultLabels = new ArrayList<>();

        for (SearchCondition condition : searchConditions) {
            String matchPattern = condition.getOperand() != null ? String.valueOf(condition.getOperand()).trim() : "";
            Label label = Label.fromField(account.getFolderByName(matchPattern, null, null).getFolderName());
            resultLabels.add(label);
        }
        return resultLabels;
    }

    @Override
    public long count(List<SearchCondition> list) {
        return 0;
    }

    @Override
    public List<String> lookup(Map<String, Object> map, List<String> list) {
        return List.of();
    }
}
