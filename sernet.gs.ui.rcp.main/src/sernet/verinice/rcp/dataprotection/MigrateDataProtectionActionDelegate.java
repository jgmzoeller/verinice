/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.dataprotection;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.RightsEnabledActionDelegate;
import sernet.verinice.rcp.dialogs.ScrollableMultilineDialog;
import sernet.verinice.service.commands.dataprotection.migration.MigrateDataProtectionCommand;

/**
 * The action to start a data protection migration.
 */
public class MigrateDataProtectionActionDelegate extends RightsEnabledActionDelegate
        implements IWorkbenchWindowActionDelegate {

    private final class RunMigrationCommand implements IRunnableWithProgress {
        private final Set<CnATreeElement> selectedElementSet;
        private MigrateDataProtectionCommand migrateDataProtectionCommand;

        private RunMigrationCommand(Set<CnATreeElement> selectedElementSet) {
            this.selectedElementSet = selectedElementSet;
        }

        @Override
        public void run(IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
            monitor.beginTask(Messages.MigrateDataProtectionActionDelegate_monitor_message,
                    IProgressMonitor.UNKNOWN);
            MigrateDataProtectionCommand command = new MigrateDataProtectionCommand(
                    selectedElementSet);

            try {
                migrateDataProtectionCommand = ServiceFactory.lookupCommandService()
                        .executeCommand(command);
                monitor.beginTask(Messages.MigrateDataProtectionActionDelegate_monitor_message_refresh, IProgressMonitor.UNKNOWN);
                CnAElementFactory.getInstance().reloadIsoModelFromDatabase();
            } catch (CommandException e) {
                LOG.error("Error while migrating dataprotection", e); //$NON-NLS-1$
            }
            monitor.done();
        }
    }

    private static final Logger LOG = Logger.getLogger(MigrateDataProtectionActionDelegate.class);

    private CnATreeElement selectedOrganization;

    private Shell shell;

    @Override
    public void doRun(IAction action) {
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /* ask save */);
            MigrateDataProtectionDialog dialog = new MigrateDataProtectionDialog(getShell());
            dialog.setSelectedElement(selectedOrganization);
            boolean showMigrationDialog = dialog.isShowMigrationDialog();
            if (dialog.open() == Window.OK) {
                Set<CnATreeElement> selectedElementSet = dialog.getSelectedElementSet();
                RunMigrationCommand commandRunner = new RunMigrationCommand(selectedElementSet);
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(commandRunner);

                if (showMigrationDialog) {
                    MigrateDataProtectionCommand cmd = commandRunner.migrateDataProtectionCommand;
                    Set<String> processes = cmd.getAffectedProcessNames();
                    // Set<String> controls = cmd.getAffectedControlsNames();
                    Set<String> missedControls = cmd.getMissedControlNames();
                    displayFinishedDialog(selectedElementSet, processes,
                            cmd.getAffectedNumberOfControls(), missedControls,
                            cmd.getNumberOfCreatedLinks(), cmd.getNumberOfDeletedLinks());
                }
            }
        } catch (Exception e) {
            LOG.error("Error running the dataprotection migration.", e); //$NON-NLS-1$
            MessageDialog.openError(getShell(), "Error while migrating dataprotection", //$NON-NLS-1$
                    "An error occours");//$NON-NLS-1$
        }
    }

    private void displayFinishedDialog(Set<CnATreeElement> organizations,
            Set<String> processes, int controls, Set<String> missedControls,
            int createdLinks, int deletedLinks) {
        String listOfMissedControls = StringUtils.join(missedControls, "\n");
        String processNames = StringUtils.join(processes, ", ");

        StringBuilder orgNames = new StringBuilder();
        for (CnATreeElement org : organizations) {
            orgNames.append(org.getTitle()).append(" "); //$NON-NLS-1$
        }

        String message = Messages.bind(
                organizations.size() == 1
                        ? Messages.MigrateDataProtectionActionDelegate_migration_log_singular
                        : Messages.MigrateDataProtectionActionDelegate_migration_log_plural,
                new String[] { orgNames.toString(), Integer.toString(processes.size()),
                        Integer.toString(controls),
                        Integer.toString(createdLinks), Integer.toString(deletedLinks),
                        processNames, Integer.toString(missedControls.size()),
                        listOfMissedControls });

        ScrollableMultilineDialog multilineDialog = new ScrollableMultilineDialog(
                Display.getCurrent().getActiveShell(),
                message, Messages.MigrateDataProtectionActionDelegate_migration_finished_title,
                Messages.MigrateDataProtectionActionDelegate_migration_finished_message);
        multilineDialog.open();
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.MIGRATE_DATA_PROTECTION;
    }

    @Override
    public void init(IWorkbenchWindow window) {
        try {
            shell = window.getShell();
        } catch (Exception e) {
            LOG.error("Error creating ActionDelegate", e); //$NON-NLS-1$
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {//TODO use multi selection
        if (selection instanceof ITreeSelection) {
            selectedOrganization = null;
            ITreeSelection selectionCurrent = (ITreeSelection) selection;
            for (Iterator<?> iter = selectionCurrent.iterator(); iter.hasNext();) {
                Object selectedObject = iter.next();
                if (isOrganization(selectedObject)) {
                    selectedOrganization = (CnATreeElement) selectedObject;
                }
            }
        }
    }
    private boolean isOrganization(Object element) {
        return element instanceof Organization;
    }

    private Shell getShell() {
        return shell;
    }
}
