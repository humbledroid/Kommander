package com.humbledroid.kommander;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.components.*;
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Service(Service.Level.PROJECT)
@State(
    name = "KustomCommandState",
    storages = [Storage("kommander.xml")]
)
class KustomCommandState : PersistentStateComponent<KustomCommandState.State> {

    data class CommandItem(
        var name: String = "",
        var command: String = "",
        var workingDir: String = ""
    )

    class State {
        var commands: MutableList<CommandItem> = mutableListOf()
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun getCommands(): ImmutableList<CommandItem> = myState.commands.toImmutableList()

    fun addCommand(command: CommandItem) {
        myState.commands.add(command)
    }

    fun removeCommand(index: Int) {
        if (index >= 0 && index < myState.commands.size) {
            myState.commands.removeAt(index)
        }
    }

    fun updateCommand(index: Int, command: CommandItem) {
        if (index >= 0 && index < myState.commands.size) {
            myState.commands[index] = command
        }
    }

    companion object {
        fun getInstance(project: Project): KustomCommandState = project.service()
    }
}