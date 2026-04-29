import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { Layout, Menu } from 'antd'
import {
  FileTextOutlined,
  PlusOutlined,
  ClockCircleOutlined,
  BarChartOutlined,
  WarningOutlined,
} from '@ant-design/icons'
import { Link, useLocation } from 'react-router-dom'
import CaseList from './pages/CaseList'
import NewCase from './pages/NewCase'
import CaseDetail from './pages/CaseDetail'
import Reports from './pages/Reports'
import OverdueCases from './pages/OverdueCases'

const { Header, Sider, Content } = Layout

const menuItems = [
  {
    key: '/cases',
    icon: <FileTextOutlined />,
    label: <Link to="/cases">办件列表</Link>,
  },
  {
    key: '/cases/new',
    icon: <PlusOutlined />,
    label: <Link to="/cases/new">新建办件</Link>,
  },
  {
    key: '/overdue',
    icon: <WarningOutlined />,
    label: <Link to="/overdue">超期督办</Link>,
  },
  {
    key: '/reports',
    icon: <BarChartOutlined />,
    label: <Link to="/reports">统计报表</Link>,
  },
]

function AppLayout({ children }) {
  const location = useLocation()

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ padding: '0 24px', display: 'flex', alignItems: 'center' }}>
        <div style={{ color: 'white', fontSize: '20px', fontWeight: 'bold' }}>
          <ClockCircleOutlined style={{ marginRight: '12px' }} />
          政务办件管理系统
        </div>
      </Header>
      <Layout>
        <Sider width={200} theme="light">
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            style={{ height: '100%', borderRight: 0 }}
            items={menuItems}
          />
        </Sider>
        <Layout style={{ padding: '24px' }}>
          <Content
            style={{
              background: '#fff',
              padding: 24,
              margin: 0,
              minHeight: 280,
              borderRadius: 6,
            }}
          >
            {children}
          </Content>
        </Layout>
      </Layout>
    </Layout>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AppLayout>
        <Routes>
          <Route path="/" element={<CaseList />} />
          <Route path="/cases" element={<CaseList />} />
          <Route path="/cases/new" element={<NewCase />} />
          <Route path="/cases/:id" element={<CaseDetail />} />
          <Route path="/overdue" element={<OverdueCases />} />
          <Route path="/reports" element={<Reports />} />
        </Routes>
      </AppLayout>
    </BrowserRouter>
  )
}

export default App
