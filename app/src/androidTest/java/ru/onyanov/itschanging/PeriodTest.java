package ru.onyanov.itschanging;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import ru.onyanov.itschanging.helpers.PeriodHelper;

public class PeriodTest {

    private PeriodHelper periodHelper;

    @Before
    public void createPeriodManager() {
        periodHelper = new PeriodHelper();
    }

    @After
    public void tearDown() {
        periodHelper = null;
    }

    @Test
    public void periodManager_CheckPeriod_ReturnsNotNull() {
        assertNotNull(periodHelper);
    }

    @Test
    public void periodManager_CheckPeriod_PeriodHasType() {
        assertNotNull(periodHelper.getType());
    }

    @Test
    public void periodManager_CheckPeriod_PeriodTypeIsMonth() {
        periodHelper.setType(PeriodHelper.TYPE_MONTH);
        assertEquals(periodHelper.getType(), "month");
    }

    @Test
    public void periodManager_CheckPeriod_NotExceedMaxValue() {
        periodHelper.setValue(40);
        periodHelper.setType(PeriodHelper.TYPE_MONTH);
        assertEquals(PeriodHelper.MAX_VALUE_MONTH, periodHelper.getValue());
    }


}
