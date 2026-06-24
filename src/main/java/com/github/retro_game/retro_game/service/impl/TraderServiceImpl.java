package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.TradeOfferDto;
import com.github.retro_game.retro_game.dto.TradeResourcesParamsDto;
import com.github.retro_game.retro_game.entity.Resources;
import com.github.retro_game.retro_game.service.TraderService;
import com.github.retro_game.retro_game.service.exception.NotEnoughResourcesException;
import com.github.retro_game.retro_game.service.exception.WrongTradeAmountException;
import com.github.retro_game.retro_game.service.exception.WrongTradedResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

@Service
class TraderServiceImpl implements TraderService {
  private static final String[] RESOURCES = {"Metal", "Crystal", "Deuterium"};
  private static final String[] RESOURCE_KEYS = {"metal", "crystal", "deuterium"};
  private final BodyServiceInternal bodyServiceInternal;
  private static final Logger logger = LoggerFactory.getLogger(TraderServiceImpl.class);

  TraderServiceImpl(BodyServiceInternal bodyServiceInternal) {
    this.bodyServiceInternal = bodyServiceInternal;
  }

  @Override
  public TradeOfferDto getCurrentOffer() {
    long seed = LocalDate.now().toEpochDay();
    var random = new Random(seed);
    int tradedResourceIndex = random.nextInt(RESOURCES.length);

    double crystalRate = 1.6 + random.nextDouble() * (2.1 - 1.6);
    double metalRate = 2.4 + random.nextDouble() * (3.2 - 2.4);

    return new TradeOfferDto(
        RESOURCES[tradedResourceIndex],
        RESOURCE_KEYS[tradedResourceIndex],
        metalRate,
        crystalRate,
        1.0
    );
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void trade(TradeResourcesParamsDto params) {
    logger.info("Trade request received.");
    var offer = getCurrentOffer();
    if (!offer.getTradedResourceKey().equals(params.getTradedResourceKey())) {
      throw new WrongTradedResourceException();
    }

    double metalAmount = Math.floor(Math.max(0.0, params.getMetalAmount()));
    double crystalAmount = Math.floor(Math.max(0.0, params.getCrystalAmount()));
    double deuteriumAmount = Math.floor(Math.max(0.0, params.getDeuteriumAmount()));

    switch (offer.getTradedResourceKey()) {
      case "metal" -> metalAmount = 0.0;
      case "crystal" -> crystalAmount = 0.0;
      case "deuterium" -> deuteriumAmount = 0.0;
      default -> throw new WrongTradedResourceException();
    }

    double tradedResourceRate = getRateByKey(offer, offer.getTradedResourceKey());
    double receivedAmount = metalAmount * (tradedResourceRate / offer.getMetalRate()) +
        crystalAmount * (tradedResourceRate / offer.getCrystalRate()) +
        deuteriumAmount * (tradedResourceRate / offer.getDeuteriumRate());
    receivedAmount = Math.floor(receivedAmount * 100.0) / 100.0;

    double expectedReceivedAmount = Math.floor(Math.max(0.0, params.getExpectedReceivedAmount()) * 100.0) / 100.0;
    if (Math.abs(receivedAmount - expectedReceivedAmount) > 0.001) {
      throw new WrongTradeAmountException();
    }

    var body = bodyServiceInternal.getUpdated(params.getBodyId());
    Resources resources = body.getResources();

    if (resources.getMetal() < metalAmount || resources.getCrystal() < crystalAmount ||
        resources.getDeuterium() < deuteriumAmount) {
      throw new NotEnoughResourcesException();
    }

    resources.setMetal(resources.getMetal() - metalAmount);
    resources.setCrystal(resources.getCrystal() - crystalAmount);
    resources.setDeuterium(resources.getDeuterium() - deuteriumAmount);

    switch (offer.getTradedResourceKey()) {
      case "metal" -> resources.setMetal(resources.getMetal() + receivedAmount);
      case "crystal" -> resources.setCrystal(resources.getCrystal() + receivedAmount);
      case "deuterium" -> resources.setDeuterium(resources.getDeuterium() + receivedAmount);
      default -> throw new WrongTradedResourceException();
    }
  }

  private double getRateByKey(TradeOfferDto offer, String key) {
    return switch (key) {
      case "metal" -> offer.getMetalRate();
      case "crystal" -> offer.getCrystalRate();
      case "deuterium" -> offer.getDeuteriumRate();
      default -> throw new WrongTradedResourceException();
    };
  }
}
