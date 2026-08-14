import React, { useState } from 'react';
import UserManagementTab from './UserManagementTab';
import ContainerManagementTab from './ContainerManagementTab';
import MetricsDashboardTab from './MetricsDashboardTab';
import ReportsTab from './ReportsTab';

export default function AdminPanelScreen() {
  const [activeTab, setActiveTab] = useState('metrics');

  return (
    <div className="admin-panel">
      <div className="admin-nav-bar">
        <button
          className={`admin-tab-btn ${activeTab === 'metrics' ? 'active' : ''}`}
          onClick={() => setActiveTab('metrics')}
        >
          📈 Dashboard & Métricas
        </button>
        <button
          className={`admin-tab-btn ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          👥 Gestión de Usuarios
        </button>
        <button
          className={`admin-tab-btn ${activeTab === 'containers' ? 'active' : ''}`}
          onClick={() => setActiveTab('containers')}
        >
          📦 Contenedores
        </button>
        <button
          className={`admin-tab-btn ${activeTab === 'reports' ? 'active' : ''}`}
          onClick={() => setActiveTab('reports')}
        >
          📁 Reportes & Excel ZIP
        </button>
      </div>

      <div className="admin-card">
        {activeTab === 'metrics' && <MetricsDashboardTab />}
        {activeTab === 'users' && <UserManagementTab />}
        {activeTab === 'containers' && <ContainerManagementTab />}
        {activeTab === 'reports' && <ReportsTab />}
      </div>
    </div>
  );
}
