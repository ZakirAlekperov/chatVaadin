package com.zakalek.chat;

import com.github.rjeschke.txtmark.Processor;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

@Route("")
@Push
public class MainView extends VerticalLayout implements AppShellConfigurator {

    private final Storage storage;
    private Registration registration;
    private Grid<Storage.ChatMessage> grid;
    private VerticalLayout login;
    private VerticalLayout chat;
    private String user = "";
    
    public MainView(Storage storage) {
        this.storage = storage;
        buildLogin();
        buildChat();

    }

    private void buildLogin() {
        login = new VerticalLayout(){
            {
                TextField field =new TextField();
                field.setPlaceholder("Please, introduce yourself");
                add(
                        field,
                        new Button("Login"){
                            {
                                addClickListener( clicl -> {
                                    login.setVisible(false);
                                    chat.setVisible(true);
                                    user = field.getValue();
                                    storage.addRecordJoined(user);
                                });

                                addClickShortcut(Key.ENTER);
                            }
                        }
                );
            }
        };

        add(login);
    }

    private void buildChat() {

        chat = new VerticalLayout();
        add(chat);
        chat.setVisible(false);

        grid = new Grid<>();
        grid.setItems(storage.getMessages());
        grid.addColumn(new ComponentRenderer<>(message -> new Html(renderRow(message))))
                .setAutoWidth(true);

        TextField textField = new TextField();

        Button sendMessage = new Button("->") {
            {
                addClickListener( click -> {
                    storage.addRecord(user, textField.getValue());
                    textField.clear();
                });
                addClickShortcut(Key.ENTER);
            }
        };
        chat.add(
                new H3("Vaadin chat"),

                grid,
                new HorizontalLayout(){{
                    add(
                            textField,
                            sendMessage
                    );
                }}

        );
    }

    private static String renderRow(Storage.ChatMessage message) {
        if(message.getName().isEmpty()){
            return Processor.process(String.format("User **%s** is just joined the chat!_"
                    ,message.getMessage()));
        }else {
            return Processor.process(String.format("**%S**: %s"
                    ,message.getName()
                    ,message.getMessage()));
        }

    }


    public void onMessage(Storage.ChatEvent chatEvent){
        if(getUI().isPresent()){
            UI ui = getUI().get();
            ui.getSession().lock();
            ui.beforeClientResponse(grid, ctx-> grid.scrollToEnd());
            ui.access(() -> grid.getDataProvider().refreshAll());
            ui.getSession().unlock();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
         registration = storage.attachListener(this::onMessage);
    }
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        registration.remove();
    }
}
