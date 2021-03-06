package com.infoshareacademy.jjdd6.servlet;

import com.infoshareacademy.jjdd6.dao.ShareDao;
import com.infoshareacademy.jjdd6.dao.TransactionDao;
import com.infoshareacademy.jjdd6.dao.WalletDao;
import com.infoshareacademy.jjdd6.freemarker.TemplateProvider;
import com.infoshareacademy.jjdd6.service.StatsService;
import com.infoshareacademy.jjdd6.service.TickerService;
import com.infoshareacademy.jjdd6.service.UserService;
import com.infoshareacademy.jjdd6.validation.Validators;
import com.infoshareacademy.jjdd6.wilki.*;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/share-buy")
@Transactional
public class BuySharesServlet extends HttpServlet {

    private static Logger logger = LoggerFactory.getLogger(BuySharesServlet.class);

    @Inject
    private WalletDao walletDao;

    @Inject
    private ShareDao shareDao;

    @Inject
    private TransactionDao transactionDao;

    @Inject
    private UserService userService;

    @Inject
    private Validators validators;

    @Inject
    private TemplateProvider templateProvider;

    @Inject
    private StatsService statsService;

    @Inject
    private TickerService tickerService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        showMenuWithBuyForm(req, resp, "");

    }

    private void showMenuWithBuyForm(HttpServletRequest req, HttpServletResponse resp, String status) throws IOException {
        Map<String, Object> model = new HashMap<>();

        User user = userService.loggedUser(req);
        Wallet userWallet = user.getWallet();
        BigDecimal roe = userWallet.getROE();
        BigDecimal freeCash = userWallet.getFreeCash();
        String profilePicURL = userService.userProfilePicURL(user);
        Map<String, String> bestPerforming = statsService.getMostProfitableShare(userWallet);
        Map<String, String> worstPerforming = statsService.getLeastProfitableShare(userWallet);
        List<Ticker> tickers = tickerService.getAll();
        int userAdmin = 0;
        if (user.isAdmin()) {
            userAdmin = 1;
        }
        model.put("tickers", tickers);
        model.put("isAdmin", userAdmin);
        model.put("mpTicker", bestPerforming.get("ticker"));
        model.put("mpProfit", bestPerforming.get("profit"));
        model.put("mpReturn", bestPerforming.get("return"));
        model.put("wpTicker", worstPerforming.get("ticker"));
        model.put("wpProfit", worstPerforming.get("profit"));
        model.put("wpReturn", worstPerforming.get("return"));
        model.put("roe", roe);
        model.put("freeCash", freeCash);
        model.put("content", "buy_shares");
        model.put("profilePicURL", profilePicURL);
        model.put("userName", user.getName());
        if (null != status) {
            model.put("status", status);
        }

        Template template = templateProvider.getTemplate(getServletContext(), "menu.ftlh");

        try {
            template.process(model, resp.getWriter());
        } catch (TemplateException e) {
            resp.getWriter().println("Something went wrong");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        buyShare(req, resp);
    }

    private void buyShare(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String ticker = req.getParameter("ticker");

        if (validators.isTickerNotValid(ticker)) {
            showMenuWithBuyForm(req, resp,"Ticker " + ticker.toUpperCase() + " is not valid");
            logger.info("Ticker {} is not valid.", ticker);
            return;
        }

        String amountStr = req.getParameter("amount");

        if (validators.isNotIntegerOrIsSmallerThanZero(amountStr)) {
            showMenuWithBuyForm(req, resp,"Amount should be an integer greater than 0");
            logger.info("Incorrect amount = {}", amountStr);
            return;
        }

        String priceStr = req.getParameter("price");

        if (validators.isNotDoubleOrIsSmallerThanZero(priceStr)) {
            showMenuWithBuyForm(req, resp,"Price should be a number greater than 0 - format 0.0000");
            logger.info("Incorrect price = {}", priceStr);
           return;
        }

        User user = userService.loggedUser(req);
        int amount = Integer.parseInt(amountStr);
        double price = Double.parseDouble(priceStr);
        final Wallet existingWallet = user.getWallet();

        if (validators.isEnoughCashToBuyShares(existingWallet, amount, price)) {
            showMenuWithBuyForm(req, resp,"You don't have enough money!");
            logger.info("Not enough money to buy shares");
            return;
        }

        existingWallet.buyShare(ticker, amount, price);
        Transaction transaction = existingWallet.scanWalletForShare(ticker).getTransactionHistory().get(existingWallet.scanWalletForShare(ticker).getTransactionHistory().size() - 1);
        Share share = existingWallet.scanWalletForShare(ticker);
        share.setFullCompanyName(tickerService.scanTickers(ticker));
        transaction.setShare(share);
        transaction.setWallet(existingWallet);

        transactionDao.save(transaction);
        shareDao.save(share);
        walletDao.update(existingWallet);

        logger.info("Wallet object updated: {}", existingWallet);

        logger.info("Transaction success." + "\nFree Cash: " + existingWallet.getFreeCash());
        showMenuWithBuyForm(req, resp, "Transaction success");
    }
}

