package server;

import common.DataManager;
import common.network.CommandResult;
import common.network.Request;


import javax.xml.bind.JAXBException;
import java.util.HashMap;

/**
 * The class is responsible for calling commands
 */
public class Service {
    private HashMap<String, Executable> commands = new HashMap<>();
    private PersonCollection collection = new PersonCollection();
    private DataManager dataManager;

    public Service(DataManager dataManager) {
        this.dataManager = dataManager;
        initCommands();
    }

    /**
     * add commands with a link to the method
     */
    private void initCommands() {
        commands.put("add", dataManager::add);
        commands.put("add_if_max", dataManager::addIfMax);
        commands.put("add_if_min", dataManager::addIfMin);
        commands.put("show", dataManager::show);
        commands.put("clear", dataManager::clear);
        commands.put("info", dataManager::info);
        commands.put("help", dataManager::help);
        commands.put("count_greater_than_eye_color", dataManager::countEyeColor);
        commands.put("filter_greater_than_location", dataManager::filterGreater);
        commands.put("print_unique_location", dataManager::printUniqueLocation);
        commands.put("remove_by_id", dataManager::remove_by_id);
        commands.put("remove_greater", dataManager::removeGreater);
        commands.put("update", dataManager::update);
        commands.put("exit", dataManager::exit);

    }

    /**
     * check if there is a command on the server and execute it
     *
     * @param request request - command from client
     */
    public CommandResult executeCommand(Request<?> request) throws JAXBException {
        if (!commands.containsKey(request.command) && request.command != null)
            //request.personCollection.getCollection().size()!=0
            return new CommandResult(false, "Такой команды на сервере нет.");
        else if (request.command == null && request.personCollection != null) {
            collection.loadCollection(request.personCollection.getCollection());
            return new CommandResult(true, "правда");
        } else if (request.command == null) {
            if (collection.toHeight((int) request.type) || collection.existID((int) request.type)) {
                return new CommandResult(true, "правда");
            } else {
                return new CommandResult(false, "неправда");
            }
        }
        return commands.get(request.command).execute(request);
    }
}