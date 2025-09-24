package app.krista.extensions.essentials.collaboration.outlook3.impl;

import app.krista.extensions.essentials.collaboration.outlook3.impl.util.EntityHelperUtil;
import app.krista.extensions.essentials.collaboration.outlook3.impl.util.FilenameUtil;
import app.krista.model.base.EntityValue;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class EntityHelperUtilTest {

    public EntityHelperUtilTest() {
    }

    @Test
    public void testGetValidatedData() {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("name", "Naman");
        entityMap.put("age", 25);
        entityMap.put("isStudent", Boolean.TRUE);
        entityMap.put("hasJob", Boolean.FALSE);

        Map<String, Object> validatedEntityMap = EntityHelperUtil.getValidatedData(entityMap);

        Assert.assertEquals("Naman", validatedEntityMap.get("name"));
        Assert.assertEquals(25, validatedEntityMap.get("age"));
        Assert.assertEquals("Yes", validatedEntityMap.get("isStudent"));
        Assert.assertEquals("No", validatedEntityMap.get("hasJob"));
    }

    @Test
    public void testFetchDateTimeForDateKey() {
        long timestamp = 1630080000000L; // August 28, 2021 12:00 AM
        String key = "event_date";
        String formattedDate = EntityHelperUtil.fetchDateTime(timestamp, key);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        String expectedFormattedDate = sdf.format(new Date(timestamp));

        Assert.assertEquals(expectedFormattedDate, formattedDate);
    }

    @Test
    public void testFetchDateTimeForTimeKey() {
        long timestamp = 1630147200000L; //8:00 AM
        String key = "start_time";
        String formattedTime = EntityHelperUtil.fetchDateTime(timestamp, key);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String expectedFormattedTime = sdf.format(new Date(timestamp));

        Assert.assertEquals(expectedFormattedTime, formattedTime);
    }

    @Test
    public void testGetMessageContentWithHTML() {
        List<EntityValue> entityValues = new ArrayList<>();
        Map<String, Object> fields1 = new HashMap<>();
        fields1.put("Name", "Aman");
        fields1.put("Numb", 0);
        fields1.put("isStudent", false);

        Map<String, Object> fields2 = new HashMap<>();
        fields2.put("Name", "Naman");
        fields2.put("Numb", 123);
        fields2.put("isStudent", true);
        EntityValue entity1 = new EntityValue("TestEntity", fields1);
        EntityValue entity2 = new EntityValue("TestEntity", fields2);
        entityValues.add(entity1);
        entityValues.add(entity2);

        String actualOutput = EntityHelperUtil.getMessageContent("Simple Message Body", entityValues, null,null);
        String expectedFormat = "<html><head><style>table {width: 100%; border-collapse: collapse;} th, td {border: 1px solid #dddddd; text-align: left; padding: 8px;}</style></head><body><div><br/>Simple Message Body</div><br/><table><tr><th>Numb</th><th>isStudent</th><th>Name</th></tr><tr><td>0</td><td>No</td><td>Aman</td></tr><tr><td>123</td><td>Yes</td><td>Naman</td></tr></table><br/></body></html>";
        System.out.println(" actualOutput "+actualOutput);
        System.out.println(" expectedFormat "+expectedFormat);
        Assert.assertEquals(expectedFormat, actualOutput);
    }

    @Test
    public void testGetMessageContentWithText() {
        List<EntityValue> entityValues = new ArrayList<>();
        Map<String, Object> fields1 = new HashMap<>();
        fields1.put("Name", "Aman");
        fields1.put("Numb", 0);
        fields1.put("isStudent", false);

        Map<String, Object> fields2 = new HashMap<>();
        fields2.put("Name", "Naman");
        fields2.put("Numb", 123);
        fields2.put("isStudent", true);
        EntityValue entity1 = new EntityValue("TestEntity", fields1);
        EntityValue entity2 = new EntityValue("TestEntity", fields2);
        entityValues.add(entity1);
        entityValues.add(entity2);

        String actualOutput = EntityHelperUtil.getMessageContent("Simple Message Body", entityValues, null,null);
        String expectedFormat = "<html><head><style>table {width: 100%; border-collapse: collapse;} th, td {border: 1px solid #dddddd; text-align: left; padding: 8px;}</style></head><body><div><br/>Simple Message Body</div><br/><table><tr><th>Numb</th><th>isStudent</th><th>Name</th></tr><tr><td>0</td><td>No</td><td>Aman</td></tr><tr><td>123</td><td>Yes</td><td>Naman</td></tr></table><br/></body></html>";
        System.out.println(" actualOutput "+actualOutput);
        System.out.println(" expectedFormat "+expectedFormat);
        Assert.assertEquals(expectedFormat, actualOutput);
    }

    @Test
    public void testRemoveTrailingZerosWithDecimals() {
        double number = 123.456000;
        String result = EntityHelperUtil.removeTrailingZeros(number);
        Assert.assertEquals("123.456", result);
    }

    @Test
    public void testRemoveTrailingZerosWithoutDecimals() {
        double number = 100.000;
        String result = EntityHelperUtil.removeTrailingZeros(number);
        Assert.assertEquals("100", result);
    }

    @Test
    public void testRemoveTrailingZerosInteger() {
        double number = 12345.0;
        String result = EntityHelperUtil.removeTrailingZeros(number);
        Assert.assertEquals("12345", result);
    }

    @Test
    public void testFilenameUtil_Sample() {
        String input = "Clyde’s Animal Clinic - Client Service Agreement - Evette 2024 - signed.pdf";
        String output = FilenameUtil.toSafeFilename(input);
        Assert.assertTrue(output.endsWith(".pdf"));
        Assert.assertFalse(output.contains("’"));
    }

}
