/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2013, 6WIND S.A. All rights reserved.                 *
 *                                                                     *
 * This file is part of the Jenkins Lockable Resources Plugin and is   *
 * published under the MIT license.                                    *
 *                                                                     *
 * See the "LICENSE.txt" file for more information.                    *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.jenkins.plugins.lockableresources.actions;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.User;
import hudson.security.AccessDeniedException2;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;

import org.jenkins.plugins.lockableresources.LockableResource;
import org.jenkins.plugins.lockableresources.LockableResourcesManager;
import org.jenkins.plugins.lockableresources.Messages;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class LockableResourcesRootAction implements RootAction {

	public static final PermissionGroup PERMISSIONS_GROUP = new PermissionGroup(
			LockableResourcesManager.class, Messages._LockableResourcesRootAction_PermissionGroup());
	public static final Permission UNLOCK = new Permission(PERMISSIONS_GROUP,
			Messages.LockableResourcesRootAction_UnlockPermission(),
			Messages._LockableResourcesRootAction_UnlockPermission_Description(), Jenkins.ADMINISTER,
			PermissionScope.JENKINS);
	public static final Permission RESERVE = new Permission(PERMISSIONS_GROUP,
			Messages.LockableResourcesRootAction_ReservePermission(),
			Messages._LockableResourcesRootAction_ReservePermission_Description(), Jenkins.ADMINISTER,
			PermissionScope.JENKINS);

	public static final Permission VIEW = new Permission(PERMISSIONS_GROUP,
			Messages.LockableResourcesRootAction_ViewPermission(),
			Messages._LockableResourcesRootAction_ViewPermission_Description(), Jenkins.ADMINISTER,
			PermissionScope.JENKINS);
	
	public static final String ICON = "/plugin/lockable-resources/img/device-24x24.png";

	public String getIconFileName() {
		return (Jenkins.getInstance().hasPermission(VIEW)) ? ICON : null;
	}

	public String getUserName() {
		User current = User.current();
		if (current != null)
			return current.getFullName();
		else
			return null;
	}

	public String getDisplayName() {
		return "Lockable Resources";
	}

	public String getUrlName() {
		return (Jenkins.getInstance().hasPermission(VIEW)) ? "lockable-resources" : "";
	}

	public List<LockableResource> getResources() {
		return LockableResourcesManager.get().getResources();
	}

	public int getFreeResourceAmount(String label) {
		return LockableResourcesManager.get().getFreeResourceAmount(label);
	}

	public Set<String> getAllLabels() {
		return LockableResourcesManager.get().getAllLabels();
	}

	public int getNumberOfAllLabels() {
		return LockableResourcesManager.get().getAllLabels().size();
	}

	public void doUnlock(StaplerRequest req, StaplerResponse rsp)
			throws IOException, ServletException {
		Jenkins.getInstance().checkPermission(UNLOCK);

		String name = req.getParameter("resource");
		LockableResource r = LockableResourcesManager.get().fromName(name);
		if (r == null) {
			rsp.sendError(404, "Resource not found " + name);
			return;
		}

		List<LockableResource> resources = new ArrayList<>();
		resources.add(r);
		LockableResourcesManager.get().unlock(resources, null);

		rsp.forwardToPreviousPage(req);
	}

	public void doReserve(StaplerRequest req, StaplerResponse rsp)
		throws IOException, ServletException {
		Jenkins.getInstance().checkPermission(RESERVE);
		///< Barrelfish Testing Infrastructure Extension	
		String name = req.getParameter("resource");
		String onBehalf = req.getParameter("for");
		///< Barrelfish Testing Infrastructure Extension	

		LockableResource r = LockableResourcesManager.get().fromName(name);
		if (r == null) {
			rsp.sendError(404, "Resource not found " + name);
			return;
		}

		List<LockableResource> resources = new ArrayList<>();
		resources.add(r);
		String userName = getUserName();
		if (userName != null) {
			///< Barrelfish Testing Infrastructure Extension	
			LockableResourcesManager.get().reserve(resources, userName, onBehalf);
			///< Barrelfish Testing Infrastructure Extension	
		}
		rsp.forwardToPreviousPage(req);
	}

	///< Barrelfish Testing Infrastructure Extension	
	public void doMultireserve(StaplerRequest req, StaplerResponse rsp)
			throws IOException, ServletException {
		Jenkins.getInstance().checkPermission(RESERVE);

		String[] names = req.getParameterValues("resource");
		if (names == null) {
			rsp.sendError(404, "No resource supplied");
			return;
		}

		List<LockableResource> resources = new ArrayList<LockableResource>();
		String userName = getUserName();
		resources.add(null);
		for (String name : names) {
			LockableResource r = LockableResourcesManager.get().fromName(name);
			if (r == null) {
				rsp.sendError(404, "Resource not found " + name);
				return;
			}
			resources.set(0, r);
			if (userName != null)
				LockableResourcesManager.get().reserve(resources, userName, userName);
		}

		rsp.forwardToPreviousPage(req);
	}
	///< Barrelfish Testing Infrastructure Extension	

	public void doUnreserve(StaplerRequest req, StaplerResponse rsp)
		throws IOException, ServletException {
		Jenkins.getInstance().checkPermission(RESERVE);

		///< Barrelfish Testing Infrastructure Extension	
		/// returns a list of names here!
		String[] names = req.getParameterValues("resource");
		if (names == null) {
			rsp.sendError(404, "No resource supplied");
			return;
		}

		String userName = getUserName();
		List<LockableResource> resources = new ArrayList<LockableResource>();

		for (String name : names) {
			LockableResource r = LockableResourcesManager.get().fromName(name);
			if (r == null) {
				rsp.sendError(404, "Resource not found " + name);
				return;
			}

			if ((userName == null || !userName.equals(r.getReservedBy()))
				&& !Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER))
			throw new AccessDeniedException2(Jenkins.getAuthentication(),
					RESERVE);

			resources.add(r);
		}
		
		///< Barrelfish Testing Infrastructure Extension	
		LockableResourcesManager.get().unreserve(resources);

		rsp.forwardToPreviousPage(req);
	}

	///< Barrelfish Testing Infrastructure Extension	
	public void doGetstate(StaplerRequest req, StaplerResponse rsp)
		throws IOException, ServletException {
		Jenkins.getInstance().checkPermission(RESERVE);

		PrintWriter resp = rsp.getWriter();
		resp.append('{');
		resp.append(' ');
		List<LockableResource> resources =
			LockableResourcesManager.get().getResources();

		for (int i = 0; i < resources.size(); i++) {
			LockableResource r = resources.get(i);
			if (r.getReservedBy() != null) {
				if (r.getReservedOnBehalf() != null)
					resp.format("\"%1$s\": { \"rsvd\": \"%2$s\", \"user\": \"%3$s\",  \"acquired\": \"%4$s\", \"locked\": null }",
							r.getName(), r.getReservedBy(), r.getReservedOnBehalf(), r.getReservationTime());
				else
					resp.format("\"%1$s\": { \"rsvd\": \"%2$s\", \"acquired\": \"%3$s\", \"locked\": null }",
							r.getName(), r.getReservedBy(), r.getReservationTime());
			}
			else if (r.getBuildName() != null)
				resp.format("\"%1$s\": { \"rsvd\": null, \"locked\": \"%2$s\" }",
					    r.getName(), r.getBuildName());
			else if (r.isQueued())
				resp.format("\"%1$s\": { \"rsvd\": null, \"locked\": null, \"queued\": \"%2$s\" }",
						r.getName(), r.getQueueItemProject());
			else
				resp.format("\"%1$s\": null", r.getName());

			if (i < resources.size() - 1) {
				resp.append(',');
				resp.append(' ');
			}
		}
		resp.append(' ');
		resp.append('}');
	}
	///< Barrelfish Testing Infrastructure Extension		

	public void doReset(StaplerRequest req, StaplerResponse rsp)
		throws IOException, ServletException {
		Jenkins.getInstance().checkPermission(UNLOCK);

		String name = req.getParameter("resource");
		LockableResource r = LockableResourcesManager.get().fromName(name);
		if (r == null) {
			rsp.sendError(404, "Resource not found " + name);
			return;
		}

		List<LockableResource> resources = new ArrayList<>();
		resources.add(r);
		LockableResourcesManager.get().reset(resources);

		rsp.forwardToPreviousPage(req);
	}
}
