package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.OutlookAttributes;
import app.krista.extensions.essentials.collaboration.outlook3.impl.connectors.GraphServiceClientProviderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class AccountImplTest {

    AccountImpl accountImpl;

    @InjectMocks
    GraphServiceClientProviderFactory graphServiceClientProviderFactory;

    @Mock
    OutlookAttributes outlookAttributes;

    @Before
    public void setup() {
        accountImpl = new AccountImpl(graphServiceClientProviderFactory);
    }

    @After
    public void tearDown() {
        accountImpl = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFolderByEmptyName() {
        accountImpl.getFolderByName(Collections.singletonList(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFolderByInvalidName() {
        accountImpl.getFolderByName("", false, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFolder() {
        accountImpl.getFolder("");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetEmailByInvalidId() {
        accountImpl.getEmail("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchEmail() {
        accountImpl.searchEmails("");
    }
}
