package com.grandfatherpikhto.blin

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    BleManagerTest::class,
    BleScanManagerTest::class,
)
class BlinTestSuite
