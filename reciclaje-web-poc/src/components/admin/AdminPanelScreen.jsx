import React, { useState } from 'react';
import UserManagementTab from './UserManagementTab';
import ContainerManagementTab from './ContainerManagementTab';
import MetricsDashboardTab from './MetricsDashboardTab';
import ReportsTab from './ReportsTab';

export default function AdminPanelScreen({ onLogout }) {
  const [activeTab, setActiveTab] = useState('metrics');

  const tabs = [
    { id: 'metrics', icon: '📈', label: 'Dashboard & Métricas', shortLabel: 'Métricas' },
    { id: 'users', icon: '👥', label: 'Gestión de Usuarios', shortLabel: 'Usuarios' },
    { id: 'containers', icon: '📦', label: 'Contenedores', shortLabel: 'Contenedores' },
    { id: 'reports', icon: '📁', label: 'Reportes & Excel ZIP', shortLabel: 'Reportes' }
  ];

  return (
    <div className="admin-panel">
      <nav className="admin-nav-bar" aria-label="Navegación de pestañas administrativas">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            type="button"
            className={`admin-tab-btn ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            <span className="tab-icon">{tab.icon}</span>
            <span className="tab-label-full">{tab.label}</span>
            <span className="tab-label-short">{tab.shortLabel}</span>
          </button>
        ))}
      </nav>

      <div className="admin-card">
        {activeTab === 'metrics' && <MetricsDashboardTab />}
        {activeTab === 'users' && <UserManagementTab onLogout={onLogout} />}
        {activeTab === 'containers' && <ContainerManagementTab />}
        {activeTab === 'reports' && <ReportsTab />}
      </div>
    </div>
  );
}
