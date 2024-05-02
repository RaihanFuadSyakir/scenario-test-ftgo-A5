package net.chrisrichardson.ftgo.orderservice.sagas.cancelorder;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;
import net.chrisrichardson.ftgo.accountservice.api.AccountingServiceChannels;
import net.chrisrichardson.ftgo.accountservice.api.ReverseAuthorizationCommand;
import net.chrisrichardson.ftgo.orderservice.api.OrderServiceChannels;
import net.chrisrichardson.ftgo.orderservice.sagaparticipants.BeginCancelCommand;
import net.chrisrichardson.ftgo.orderservice.sagaparticipants.ConfirmCancelOrderCommand;
import net.chrisrichardson.ftgo.orderservice.sagaparticipants.UndoBeginCancelCommand;
import net.chrisrichardson.ftgo.kitchenservice.api.BeginCancelTicketCommand;
import net.chrisrichardson.ftgo.kitchenservice.api.ConfirmCancelTicketCommand;
import net.chrisrichardson.ftgo.kitchenservice.api.KitchenServiceChannels;
import net.chrisrichardson.ftgo.kitchenservice.api.UndoBeginCancelTicketCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

import static io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder.send;

public class CancelOrderSaga implements SimpleSaga<CancelOrderSagaData> {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private SagaDefinition<CancelOrderSagaData> sagaDefinition;

  @PostConstruct
  public void initializeSagaDefinition() {
    sagaDefinition = step()
        .invokeParticipant(this::beginCancel)
        .withCompensation(this::undoBeginCancel)
        .step()
        .invokeParticipant(this::beginCancelTicket)
        .withCompensation(this::undoBeginCancelTicket)
        .step()
        .invokeParticipant(this::reverseAuthorization)
        .step()
        .invokeParticipant(this::confirmTicketCancel)
        .step()
        .invokeParticipant(this::confirmOrderCancel)
        .build();

  }

  private CommandWithDestination confirmOrderCancel(CancelOrderSagaData data) {
    logger.warn("Order " + data.getOrderId() + " cancel ticket confirmed at" + KitchenServiceChannels.COMMAND_CHANNEL);
    return send(new ConfirmCancelOrderCommand(data.getOrderId()))
        .to(OrderServiceChannels.COMMAND_CHANNEL)
        .build();

  }

  private CommandWithDestination confirmTicketCancel(CancelOrderSagaData data) {
    logger.warn("Order " + data.getOrderId() + " cancel ticket confirmed at" + KitchenServiceChannels.COMMAND_CHANNEL);
    return send(new ConfirmCancelTicketCommand(data.getRestaurantId(), data.getOrderId()))
        .to(KitchenServiceChannels.COMMAND_CHANNEL)
        .build();

  }

  private CommandWithDestination reverseAuthorization(CancelOrderSagaData data) {
    logger.warn("Order " + data.getOrderId() + " reverse account authorization at"
        + AccountingServiceChannels.accountingServiceChannel);
    return send(new ReverseAuthorizationCommand(data.getConsumerId(), data.getOrderId(), data.getOrderTotal()))
        .to(AccountingServiceChannels.accountingServiceChannel)
        .build();

  }

  private CommandWithDestination undoBeginCancelTicket(CancelOrderSagaData data) {
    logger.error("Order " + data.getOrderId() + " undo cancel ticket at" + KitchenServiceChannels.COMMAND_CHANNEL);
    return send(new UndoBeginCancelTicketCommand(data.getRestaurantId(), data.getOrderId()))
        .to(KitchenServiceChannels.COMMAND_CHANNEL)
        .build();

  }

  private CommandWithDestination beginCancelTicket(CancelOrderSagaData data) {
    logger.warn("Order " + data.getOrderId() + " begin cancel ticket at" + KitchenServiceChannels.COMMAND_CHANNEL);
    return send(new BeginCancelTicketCommand(data.getRestaurantId(), (long) data.getOrderId()))
        .to(KitchenServiceChannels.COMMAND_CHANNEL)
        .build();

  }

  private CommandWithDestination undoBeginCancel(CancelOrderSagaData data) {
    logger.error("Order " + data.getOrderId() + " undo cancel at" + OrderServiceChannels.COMMAND_CHANNEL);
    return send(new UndoBeginCancelCommand(data.getOrderId()))
        .to(OrderServiceChannels.COMMAND_CHANNEL)
        .build();
  }

  private CommandWithDestination beginCancel(CancelOrderSagaData data) {
    logger.warn("Order " + data.getOrderId() + " begin cancel order at" + OrderServiceChannels.COMMAND_CHANNEL);
    return send(new BeginCancelCommand(data.getOrderId()))
        .to(OrderServiceChannels.COMMAND_CHANNEL)
        .build();
  }

  @Override
  public SagaDefinition<CancelOrderSagaData> getSagaDefinition() {
    Assert.notNull(sagaDefinition);
    return sagaDefinition;
  }

}
