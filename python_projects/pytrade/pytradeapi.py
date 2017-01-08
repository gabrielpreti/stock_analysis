from pytrade.feed import DynamicFeed
from pytrade.persistence.sqliteprovider import SQLiteDataProvider
from datetime import datetime
from datetime import timedelta
from pytrade.broker import PytradeBroker
from pytrade.base import TradingSystem
from pyalgotrade import logger


class PytradeApi(object):
    LOGGER_NAME = "pytradeapi"

    def __init__(self, dbfilepah, username=None, tradingAlgorithmGenerator=None, codes=None, date=datetime.now(),
                 maxlen=90, debugmode=False):
        if codes is None:
            self.__codes = ["ABEV3", "BBAS3", "BBDC3", "BBDC4", "BBSE3", "BRAP4", "BRFS3", "BRKM5", "BRML3", "BVMF3",
                            "CCRO3", "CIEL3", "CMIG4", "CPFE3", "CPLE6", "CSAN3", "CSNA3", "CTIP3", "CYRE3", "ECOR3",
                            "EGIE3", "EMBR3", "ENBR3", "EQTL3", "ESTC3", "FIBR3", "GGBR4", "GOAU4", "HYPE3", "ITSA4",
                            "ITUB4", "JBSS3", "KLBN11", "KROT3", "LAME4", "LREN3", "MRFG3", "MRVE3", "MULT3", "NATU3",
                            "PCAR4", "PETR3", "PETR4", "QUAL3", "RADL3", "RENT3", "RUMO3", "SANB11", "SBSP3", "SMLE3",
                            "SUZB5", "TIMP3", "UGPA3", "USIM5", "VALE3", "VALE5", "VIVT4", "WEGE3"]
        else:
            self.__codes = codes

        self.__logger = logger.getLogger(PytradeApi.LOGGER_NAME)
        self.__dbFilePath = dbfilepah
        self.__debugMode = debugmode
        self.__tradingAlgorithmGenerator = tradingAlgorithmGenerator
        self.__currentDate = date
        self.__maxLen = maxlen

        self.initializeDataProvider()
        self.initializeFeed()

        if username is not None:
            self.initializeBroker(username)
            self.initializeStrategy()  # tradingAlgorithmGenerator must not be None

    def initializeDataProvider(self):
        self.__dataProvider = SQLiteDataProvider(self.__dbFilePath)
        if not self.__dataProvider.schemaExists():
            self.__dataProvider.createSchema()

    def initializeFeed(self):
        assert self.__dbFilePath is not None
        assert self.__currentDate is not None
        assert self.__maxLen is not None

        fromDate = self.__currentDate - timedelta(days=self.__maxLen)
        toDate = self.__currentDate + timedelta(days=5)
        self.__feed = DynamicFeed(self.__dbFilePath, self.__codes, fromDateTime=fromDate, toDateTime=toDate,
                                  maxLen=self.__maxLen)
        self.__feed.positionFeed(self.__currentDate)

    def initializeBroker(self, username):
        assert self.__feed is not None
        assert self.__dataProvider is not None
        assert username is not None

        self.__broker = PytradeBroker(feed=self.__feed, cash=self.__dataProvider.loadCash(username),
                                      orders=self.__dataProvider.loadOrders(username),
                                      shares=self.__dataProvider.loadShares(username))

    def initializeStrategy(self):
        assert self.__feed is not None
        assert self.__broker is not None
        assert self.__debugMode is not None
        assert self.__tradingAlgorithmGenerator is not None

        self.__tradingStrategy = TradingSystem(self.__feed, self.__broker, debugMode=self.__debugMode)
        self.__tradingStrategy.setAlgorithm(self.__tradingAlgorithmGenerator(self.__feed, self.__broker))

    def userExists(self, username):
        return self.__dataProvider.userExists(username)

    def initializeUser(self, cash, username):
        return self.__dataProvider.initializeUser(username, cash)

    def reinitializeUser(self, cash, username):
        return self.__dataProvider.reinitializeUser(username, cash)

    def getCash(self):
        return self.__broker.getAvailableCash()

    def getActiveMarketOrders(self):
        return self.__broker.getMarketOrdersToConfirm()

    def getActiveStopOrders(self):
        return self.__broker.getStopOrdersToConfirm()

    def getAllActiveOrders(self):
        return self.__broker.getActiveOrders()

    def getAllShares(self):
        return self.__broker.getAllShares()

    def getShares(self, instrument):
        return self.__broker.getShares(instrument)

    def getEquity(self):
        return self.__broker.getEquity()

    def persistData(self, username):
        self.__dataProvider.persistCash(username=username, cash=self.__broker.getAvailableCash())
        self.__dataProvider.persistShares(username=username, shares=self.__broker.getAllShares())
        self.__dataProvider.persistOrders(username=username, orders=self.__broker.getAllActiveOrders())

    def confirmOrder(self, order, datetime, quantity, price, commission):
        self.__broker.acceptOrder(datetime, order)
        cost, sharesDelta, resultingCash = self.__broker.calculateCostSharesDeltaAndResultingCash(order, price,
                                                                                                  quantity, commission)

        if resultingCash >= 0:
            self.__broker.handleOrderExecution(order, datetime, price, quantity, commission, resultingCash, sharesDelta)
            return True
        else:
            self.__logger.debug("Not enough cash to fill %s order [%s] for %s share/s" % (
                order.getInstrument(),
                order.getId(),
                order.getRemaining()
            ))
            return False

    def cancelOrder(self, order):
        return self.__broker.cancelOrder(order)

    def executeAnalysis(self):
        self.__feed.dispatchWithoutIncrementingDate()

    def generateFeedEvent(self):
        self.__feed.nextEvent()

    def getCurrentBarForInstrument(self, instrument):
        return self.__broker.getCurrentBarForInstrument(instrument)
