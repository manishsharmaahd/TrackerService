package com.ffdc;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.ffdc.controller.ClickController;

/**
 * Filter to provide hooks for starting transaction before controller is called
 * and commit or rollback after controller is executed This is limited only for
 * URI strting with REST. It basically relieves the simple controller of
 * responsibility of transaction management.
 * 
 * 
 * @author Manish Sharma
 *
 */
@Component
public class JPATransactionAntipatternFilter implements Filter {
		private static final Log log = LogFactory.getLog(JPATransactionAntipatternFilter.class);

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		try {

			if (((HttpServletRequest) req).getRequestURI().toString().startsWith("/REST"))
				EntityManagerWrapper.getEntityManager().getTransaction().begin();

			chain.doFilter(req, res);

			if (((HttpServletRequest) req).getRequestURI().toString().startsWith("/REST")) {
				if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()
						&& EntityManagerWrapper.getEntityManager().getTransaction().isActive())

					try {
						EntityManagerWrapper.getEntityManager().getTransaction().commit();
					} catch (javax.persistence.RollbackException ex) {
						log.error(ex.getMessage() );
					}

				EntityManagerWrapper.closeEntityManager();
			}

		} catch (Throwable e) {
			if (EntityManagerWrapper.isEntityManagerOpeninCurrentThread()
					&& EntityManagerWrapper.getEntityManager().getTransaction().isActive())
				EntityManagerWrapper.getEntityManager().getTransaction().rollback();
			EntityManagerWrapper.closeEntityManager();
			throw e;
		}
	}

	@Override
	public void destroy() {

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
